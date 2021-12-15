import java.io.*;
import java.net.*;

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
                int port = 8888;

                // ######################################################################
		        // Esta parte funciona (mais ao menos) mas não é assim que se deve fazer
		        File file = new File(args[0]);
                // data = Files.readAllBytes(file.toPath());
                // ######################################################################

                String[] files = file.list();
                byte[] yourBytes;

                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(files);
                    out.flush();
                    yourBytes = bos.toByteArray();
                }

                DatagramPacket request = new DatagramPacket(yourBytes, yourBytes.length, ip, port);
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

  		            // ######################################################################
		            // Também funciona mas não me parece correto
                    // FileOutputStream fos = new FileOutputStream(args[0] + ".txt");
                    // fos.write(inPacket.getData());
                    // System.out.println("Ficheiro Recebido");
		            // ######################################################################

                    ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    Object o;
                    try (ObjectInput in = new ObjectInputStream(bis)) {
                        o = in.readObject();
                    }

                    String[] list = (String[]) o;

                    for(String l : list) {
                        System.out.println("Ficheiro: " + l);
                    }

                    ClientHandler ch = new ClientHandler(inPacket);
                    Thread t = new Thread(ch);
                    t.start();
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
