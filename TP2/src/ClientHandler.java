import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.util.*;

public class ClientHandler implements Runnable {

    private DatagramPacket inPacket;
    private DatagramSocket socket;
    private List<FileInfo> fileInfos;
    private File[] files;
    private String pathTo;


    public ClientHandler (DatagramPacket inPacket, List<FileInfo> fileInfos, File[] files, String pathTo) throws SocketException {
        this.inPacket = inPacket;
        this.socket = new DatagramSocket();
        this.fileInfos = fileInfos;
        this.files = files;
        this.pathTo = pathTo;
    }

    @Override
    public void run() {
        boolean running = true;

        while (running) {
            try {
                InetAddress clientIP = inPacket.getAddress();
                int port = inPacket.getPort();

                // Transforma a data recebida num PackBuilder
                PackBuilder pb = new PackBuilder().fromBytes(inPacket.getData());

                int pacote = pb.getPacote();
                // Se a PackBuilder for do TIPO1, ou seja, uma mensagem do outro peer com a
                // informação sobre os documentos que esse possui. Vamos comparar essa lista
                // com a lista de ficheiros que temos e enviar uma mensagem a pedir os que nos faltam
                if (pacote == PackBuilder.TIPO1) {
                    Object o = pb.bytesToObject();
                    @SuppressWarnings("unchecked") List<FileInfo> fileInfos2 = (List<FileInfo>) o;

                    List<String> aPedir = FileInfo.neededToSend(fileInfos, fileInfos2);

                    byte[] yourBytes = PackBuilder.objectToData(aPedir);
                    PackBuilder pbaux = new PackBuilder(PackBuilder.TIPO2, "", 0, 0, yourBytes);

                    byte[] bytes = pbaux.toBytes();
                    DatagramPacket request = new DatagramPacket(bytes, bytes.length, clientIP, port);

                    this.socket.send(request);
                    System.out.println("Files list needed sent");

                }
                // Se a PackBuilder for do TIPO2, ou seja, um request de files, vai começar o envio desses ficheiros
                else if (pacote == PackBuilder.TIPO2) {
                    Object o = pb.bytesToObject();
                    @SuppressWarnings("unchecked") List<String> filesToSend = (List<String>) o;

                    // Enviar os ficheiros
                    System.out.println("Sending files: ");
                    for (String s : filesToSend) {
                        System.out.println("---> " + s);

                        File filetoSend = null;
                        if (files != null) {
                            for (File file : files) {
                                if (file.getName().equals(s)) {
                                    filetoSend = file;
                                    break;
                                }
                            }
                        }

                        long tempoInicial = System.currentTimeMillis();

                        // Obter o conteúdo do ficheiro em bytes
                        byte[] fileContent = new byte[0];
                        if (filetoSend != null) {
                            fileContent = Files.readAllBytes(filetoSend.toPath());
                        }
                        // Dividir o byte[] em chunks
                        int chunk = 1024;
                        for (int i = 0; i < fileContent.length; i += chunk) {
                            byte[] data = Arrays.copyOfRange(fileContent, i, Math.min(fileContent.length, i + chunk));
                            PackBuilder pbChunk = new PackBuilder(PackBuilder.TIPO3, s, i / chunk, fileContent.length, data);
                            // Enviar chunk a chunk para o outro lado
                            byte[] chunkData = pbChunk.toBytes();
                            DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                            this.socket.send(request);
                        }

                        PackBuilder ack = new PackBuilder(PackBuilder.TIPO4, s, 0, 0, null);
                        byte[] chunkData = ack.toBytes();
                        DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                        this.socket.send(request);

                        long tempoFimTrasnferencia = (System.currentTimeMillis() - tempoInicial);
                        System.out.println("File " + s + " sent in " + tempoFimTrasnferencia + " miliseconds");

                        // Esperar pelo chunk de confirmação antes de enviar outro ficheiro
                        byte[] confirmation = new byte[1500];
                        DatagramPacket datagramConfirmation = new DatagramPacket(confirmation, confirmation.length);
                        this.socket.receive(datagramConfirmation);
                        // Vai ser preciso tratar desta confirmação
                        System.out.println("File confirmation received");
                    }

                    // Aqui acabam se os ficheiros por isso é preciso avisar
                    PackBuilder fin = new PackBuilder(PackBuilder.TIPO5, "", 0, 0, null);
                    byte[] chunkData = fin.toBytes();
                    DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                    this.socket.send(request);
                    System.out.println("FIN sent");

                } else if (pacote == PackBuilder.TIPO3) {
                    Map<Integer, byte[]> chunks = new TreeMap<>();
                    while (pb.getPacote() == PackBuilder.TIPO3) {
                        chunks.put(pb.getChunk(), pb.getData());

                        // Espera para receber algum pacote
                        this.socket.receive(inPacket);
                        pb = new PackBuilder().fromBytes(inPacket.getData());
                    }

                    Collection<byte[]> dataFile = chunks.values();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    for (byte[] b : dataFile) {
                        bos.write(b);
                    }
                    byte[] result = bos.toByteArray();

                    try (FileOutputStream fos = new FileOutputStream(pathTo + "/" + pb.getFilename())) {
                        fos.write(result);
                    }

                    PackBuilder confirmation = new PackBuilder(PackBuilder.TIPO4, "", 0, 0, null);
                    byte[] chunkData = confirmation.toBytes();
                    DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                    this.socket.send(request);
                    System.out.println("Confirmation sent");

                } else if (pacote == PackBuilder.TIPO5) {
                    running=false;
                    System.out.println("Recebi o FIN");
                    // Acabar conexão
                }
                this.socket.receive(this.inPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
