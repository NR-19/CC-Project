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
                int port = 8888;
                byte[] yourBytes;

                yourBytes = PackBuilder.objectToData(fileInfos);
                // Aqui vamos mandar a lista de filesInfo desta pasta
                PackBuilder pb =  new PackBuilder(PackBuilder.TIPO1, "", 0, 0, yourBytes);
                byte[] bytes = pb.toBytes();
                byte[] bytesR = new byte[1500];

                DatagramPacket request = new DatagramPacket(bytes, bytes.length, ip, port);
                DatagramPacket receive = new DatagramPacket(bytesR, bytesR.length);
                DatagramSocket socket = new DatagramSocket();
                socket.send(request);
                System.out.println("Files list sent");
                socket.setSoTimeout(2000);

                socket.receive(receive);
                ClientHandler chm = new ClientHandler(receive, fileInfos, files, args[0]);
                Thread tchm = new Thread(chm);
                tchm.start();

                //byte[] byets = ClientHandler.gerarDif(receive, fileInfos);
                //DatagramPacket request2 = new DatagramPacket(byets, byets.length, receive.getAddress(), 8888);
                //socket.send(request2);
                //byte[] byteFile = new byte[150000];
                //DatagramPacket receiveFile = new DatagramPacket(byteFile, byteFile.length);
                //socket.receive(receiveFile);

                //ClientHandler chf = new ClientHandler(receiveFile,fileInfos,);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

        //Receber pedidos de peers e responder
        new Thread(() -> {
            int port = 8888;
            System.out.println("listening on port: " + port);
            try {
                DatagramSocket serverSocket = new DatagramSocket(port);

                while(true) {
                    byte[] inBuffer = new byte[1500];
                    DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                    // Espera para receber algum pacote
                    serverSocket.receive(inPacket);

                    ClientHandler ch = new ClientHandler(inPacket, fileInfos, files, args[0]);
                    Thread cht = new Thread(ch);
                    cht.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
