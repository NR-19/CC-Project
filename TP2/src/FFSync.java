import java.io.File;
import java.io.IOException;
import java.net.*;

public class FFSync {
    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Argumentos Insuficientes");
            return;
        }

        String pasta = args[0];

        //Efetuar request a um peer no momento do run da app
        new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(args[1]);
		        String req = "Send me your files";
                byte[] data;
                int port = 8888;

		        data = req.getBytes();

                // ######################################################################
		        // Esta parte funciona (mais ao menos) mas não é assim que se deve fazer
		        File file = new File("Files/teste");
                // data = Files.readAllBytes(file.toPath());
                // ######################################################################

                String[] files = file.list();

                assert files != null;
                for(String f : files) {
                    System.out.println("Ficheiro: " + f);
                }

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

  		            // ######################################################################
		            // Também funciona mas não me parece correto
                    // FileOutputStream fos = new FileOutputStream(args[0] + ".txt");
                    // fos.write(inPacket.getData());
                    // System.out.println("Ficheiro Recebido");
		            // ######################################################################

		            System.out.println("Vou enviar os ficheiros em " + pasta
					                    + " para: " + inPacket.getAddress().toString());

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
