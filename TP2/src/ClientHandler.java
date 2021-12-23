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

    public static byte[] gerarDif(DatagramPacket receive, List<FileInfo> fileInfos) throws IOException {
        PackBuilder pb2 = PackBuilder.fromBytes(receive.getData());
        Object o = pb2.bytesToObject();
        @SuppressWarnings("unchecked") List<FileInfo> fileInfos2 = (List<FileInfo>) o;
        List<String> aPedir = FileInfo.neededToSend(fileInfos, fileInfos2);
        byte[] yourBytes2 = PackBuilder.objectToData(aPedir);
        PackBuilder pbaux = new PackBuilder(PackBuilder.TIPO2, "", 0, 0, yourBytes2);
        LogBuilder.writeLine("Gerei as diferenças entre a lista de ficheiros de "+receive.getAddress()+" e os meus ficheiros");
        return pbaux.toBytes();
    }

    public void send(InetAddress ip) throws IOException {
        byte[] yourBytes;
        yourBytes = PackBuilder.objectToData(fileInfos);
        PackBuilder pb =  new PackBuilder(PackBuilder.TIPO1, "", 0, 0, yourBytes);
        byte[] bytes = pb.toBytes();
        DatagramPacket request = new DatagramPacket(bytes, bytes.length, ip, 8888);
        DatagramSocket socket = new DatagramSocket();
        socket.send(request);
        byte[] bytesR = new byte[1500];
        DatagramPacket receive = new DatagramPacket(bytesR, bytesR.length);
        socket.receive(receive);
        this.inPacket=receive;
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

                    byte [] yourBytes = PackBuilder.objectToData(fileInfos);
                    PackBuilder toSend = new PackBuilder(PackBuilder.TIPO1, "",0,0, yourBytes);
                    byte[] send = toSend.toBytes();
                    DatagramPacket sendPacket = new DatagramPacket(send, send.length , inPacket.getAddress(), inPacket.getPort());
                    this.socket.send(sendPacket);
                    LogBuilder.writeLine("Enviei a minha lista de ficheiros a "+sendPacket.getAddress());
                    byte[] bytes = gerarDif(inPacket, fileInfos);
                    DatagramPacket request = new DatagramPacket(bytes, bytes.length, inPacket.getAddress(), port);

                    this.socket.send(request);
                    LogBuilder.writeLine("Enviei a lista de ficheiros que necessito para: "+request.getAddress());

                }
                // Se a PackBuilder for do TIPO2, ou seja, um request de files, vai começar o envio desses ficheiros
                else if (pacote == PackBuilder.TIPO2) {
                    Object o = pb.bytesToObject();
                    @SuppressWarnings("unchecked") List<String> filesToSend = (List<String>) o;

                    // Enviar os ficheiros
                    LogBuilder.writeLine("Os seguintes ficheiros foram marcados para envio:");
                    for (String s : filesToSend) {
                        LogBuilder.writeLine("---> " + s);

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
                        LogBuilder.writeLine("A iniciar o envio das chunks:");
                        for (int i = 0; i < fileContent.length; i += chunk) {
                            byte[] data = Arrays.copyOfRange(fileContent, i, Math.min(fileContent.length, i + chunk));
                            PackBuilder pbChunk = new PackBuilder(PackBuilder.TIPO3, s, i / chunk, fileContent.length, data);
                            // Enviar chunk a chunk para o outro lado
                            byte[] chunkData = pbChunk.toBytes();
                            DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                            this.socket.send(request);
                            LogBuilder.writeLine("Enviado chunk nº"+i/chunk+" para:"+request.getAddress());
                        }

                        PackBuilder ack = new PackBuilder(PackBuilder.TIPO4, s, 0, 0, null);
                        byte[] chunkData = ack.toBytes();
                        DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                        this.socket.send(request);

                        long tempoFimTrasnferencia = (System.currentTimeMillis() - tempoInicial);
                        LogBuilder.writeLine("Envio " + s + " efetuado em " + tempoFimTrasnferencia + " milisegundos");

                        // Esperar pelo chunk de confirmação antes de enviar outro ficheiro
                        byte[] confirmation = new byte[1500];
                        DatagramPacket datagramConfirmation = new DatagramPacket(confirmation, confirmation.length);
                        this.socket.receive(datagramConfirmation);
                        // Vai ser preciso tratar desta confirmação
                        LogBuilder.writeLine("Recebida confirmação de recepção.");
                    }

                    // Aqui acabam se os ficheiros por isso é preciso avisar
                    PackBuilder fin = new PackBuilder(PackBuilder.TIPO5, "", 0, 0, null);
                    byte[] chunkData = fin.toBytes();
                    DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                    this.socket.send(request);
                    LogBuilder.writeLine("Enviei a flag de finalização.");

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
                    }catch(FileNotFoundException f){

                    }

                    PackBuilder confirmation = new PackBuilder(PackBuilder.TIPO4, "", 0, 0, null);
                    byte[] chunkData = confirmation.toBytes();
                    DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                    this.socket.send(request);
                    LogBuilder.writeLine("Enviei confirmação de recepção.");

                } else if (pacote == PackBuilder.TIPO5) {
                    running=false;
                    LogBuilder.writeLine("Recebi flag de finalização.");
                    System.out.println("Processo terminado.");
                    // Acabar conexão
                }
                this.socket.receive(this.inPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
