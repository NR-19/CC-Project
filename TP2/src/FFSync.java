import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

public class FFSync {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Argumentos Insuficientes");
            return;
        }

        File pack = new File(args[0]);
        File[] files = pack.listFiles(File::isFile);

        List<FileInfo> fileInfos = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                try {
                    fileInfos.add(new FileInfo(f));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //Efetuar request a um peer no momento do run
        new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(args[1]);
                int port = 80;
                byte[] yourBytes;

                yourBytes = PackBuilder.objectToData(fileInfos);
                // Aqui vamos mandar a lista de filesInfo desta pasta
                PackBuilder pb =  new PackBuilder(PackBuilder.TIPO1, "", 0, 0, yourBytes);
                byte[] bytes = pb.toBytes();

                DatagramPacket request = new DatagramPacket(bytes, bytes.length, ip, port);
                DatagramSocket socket = new DatagramSocket();
                socket.send(request);
                System.out.println("Files list sent");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

        //Receber pedidos de peers e responder
        new Thread(() -> {
            int port = 80;
            System.out.println("listening on port: " + port);
            try {
                DatagramSocket serverSocket = new DatagramSocket(port);

                while(true) {
                    byte[] inBuffer = new byte[1500];
                    DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                    // Espera para receber algum pacote
                    serverSocket.receive(inPacket);
                    InetAddress clientIP = inPacket.getAddress();

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

                        serverSocket.send(request);
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

                            // Obter o conteúdo do ficheiro em bytes
                            byte[] fileContent = new byte[0];
                            if (filetoSend != null) {
                                fileContent = Files.readAllBytes(filetoSend.toPath());
                            }
                            // Dividir o byte[] em chunks
                            int chunk = 1024;
                            for (int  i = 0; i < fileContent.length; i+= chunk) {
                                byte[] data = Arrays.copyOfRange(fileContent, i, Math.min(fileContent.length, i + chunk));
                                PackBuilder pbChunk = new PackBuilder(PackBuilder.TIPO3, s, i / chunk, fileContent.length, data);
                                // Enviar chunk a chunk para o outro lado
                                byte[] chunkData = pbChunk.toBytes();
                                DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                                serverSocket.send(request);
                            }

                            PackBuilder ack = new PackBuilder(PackBuilder.TIPO4, s, 0, 0, null);
                            byte[] chunkData = ack.toBytes();
                            DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                            serverSocket.send(request);
                            System.out.println("File sent: " + s);

                            // Esperar pelo chunk de confirmação antes de enviar outro ficheiro
                            byte[] confirmation = new byte[1500];
                            DatagramPacket datagramConfirmation = new DatagramPacket(confirmation, confirmation.length);
                            serverSocket.receive(datagramConfirmation);
                            // Vai ser preciso tratar desta confirmação
                            System.out.println("File confirmation received");
                        }

                    } else if (pacote == PackBuilder.TIPO3) {
                        Map<Integer, byte[]> chunks = new TreeMap<>();
                        while (pb.getPacote() == PackBuilder.TIPO3) {
                            chunks.put(pb.getChunk(), pb.getData());

                            // Espera para receber algum pacote
                            serverSocket.receive(inPacket);
                            pb = new PackBuilder().fromBytes(inPacket.getData());
                        }

                        Collection<byte[]> dataFile =  chunks.values();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        for (byte[] b : dataFile) {
                            bos.write(b);
                        }
                        byte[] result =  bos.toByteArray();

                        try (FileOutputStream fos = new FileOutputStream(args[0] + pb.getFilename())) {
                            fos.write(result);
                        }

                        PackBuilder confirmation = new PackBuilder(PackBuilder.TIPO4, "", 0, 0, null);
                        byte[] chunkData = confirmation.toBytes();
                        DatagramPacket request = new DatagramPacket(chunkData, chunkData.length, clientIP, port);
                        serverSocket.send(request);
                        System.out.println("Confirmation sent");

                    } else {
                        ClientHandler ch = new ClientHandler(inPacket);
                        Thread t = new Thread(ch);
                        t.start();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
