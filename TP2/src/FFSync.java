import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class FFSync {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Argumentos Insuficientes");
            return;
        }

        File pack = new File(args[0]);
        File[] files = pack.listFiles(File::isFile);
        //listaF[0].getAbsolutePath()

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

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

        //Receber pedidos de peers e responder
        new Thread(() -> {
            int port = 80;
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

                    }
                    // Se a PackBuilder for do TIPO2, ou seja, um request de files, vai começar o envio desses ficheiros
                    else if (pacote == PackBuilder.TIPO2) {
                        Object o = pb.bytesToObject();
                        @SuppressWarnings("unchecked") List<String> filesToSend = (List<String>) o;

                        for (String s : filesToSend) {
                            System.out.println("Vou enviar o ficheiro: " + s);
                        }

                        // Aqui vai ser preciso passar a lista de Strings para uma lista de ficheiros
                        // E depois começar a enviar os ficheiros

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
