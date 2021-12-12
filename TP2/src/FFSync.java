import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class FFSync {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Argumentos Insuficientes");
            return;
        }

        //Efetuar request a um peer no momento do run da app
        new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(args[1]);
                String hello = "Hello!!";
                byte[] data = hello.getBytes();
                int port = 8888;

                DatagramPacket request = new DatagramPacket(data, data.length, ip, port);
                DatagramSocket socket = new DatagramSocket();

                socket.send(request);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }).start();

        //Receber pedidos de peers e responder
        new Thread(() -> {
            int port = 8888;
            try {
                InetAddress host = InetAddress.getLocalHost();
                String hostName = host.getHostName();
                System.out.println(hostName);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            System.out.println("Listening on port: " + port);

            try {
                DatagramSocket serverSocket = new DatagramSocket(port);

                while(true) {
                    byte[] inBuffer = new byte[1500];
                    DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                    serverSocket.receive(inPacket);

                    System.out.println("Received: " + new String(inBuffer));

                    ClientHandler ch = new ClientHandler(inPacket);
                    Thread t = new Thread(ch);
                    t.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
