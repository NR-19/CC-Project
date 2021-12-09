import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class FFSync {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Argumentos Insuficientes");
            return;
        }

        System.out.println("Pasta: " + args[0]);
        System.out.println("IP: " + args[1]);

        new Thread(() -> {
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
        }).start();
    }
}
