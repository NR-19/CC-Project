import java.io.*;
import java.net.*;
import java.util.*;

public class FFSync {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Argumentos Insuficientes");
            return;
        }

        LogBuilder.createFile();
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
                byte[] bytesR = new byte[1500];
                DatagramPacket receive = new DatagramPacket(bytesR, bytesR.length);

                ClientHandler chm = new ClientHandler(receive, fileInfos, files, args[0]);
                chm.send(ip);
                Thread tchm = new Thread(chm);
                tchm.start();

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    LogBuilder.errorLine(e.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }).start();

        //Receber pedidos de peers e responder
        new Thread(() -> {
            int port = 8888;
            try {
                LogBuilder.writeLine("Listening on port: " + port);
                DatagramSocket serverSocket = new DatagramSocket(port);

                while(true) {
                    byte[] inBuffer = new byte[1500];
                    DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                    // Espera para receber algum pacote
                    serverSocket.receive(inPacket);
                    LogBuilder.writeLine("Received packet from: " + inPacket.getAddress());

                    ClientHandler ch = new ClientHandler(inPacket, fileInfos, files, args[0]);
                    Thread cht = new Thread(ch);
                    cht.start();
                    LogBuilder.writeLine("Launched thread to deal with "+inPacket.getAddress());
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    LogBuilder.errorLine(e.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
}
