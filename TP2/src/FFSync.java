import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FFSync {
    public static void main(String[] args) {
        System.out.println("Pasta: " + args[0]);

        for (int i = 1; i < args.length; i++) {
            System.out.println(args[i]);
        }

        boolean running = true;
        int port = 80;
        System.out.println("Listening on port: " + port);

        try {
            DatagramSocket serverSocket = new DatagramSocket(port);

            while(running) {
                byte[] inBuffer = new byte[1500];
                DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                serverSocket.receive(inPacket);

                // Criar a thread
                ClientHandler ch = new ClientHandler(inPacket);
                Thread t = new Thread(ch);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
