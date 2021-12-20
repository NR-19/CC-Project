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

        File file = new File(args[0]);
        File[] listaF = file.listFiles(File::isFile);
        //listaF[0].getAbsolutePath()
        String[] files = file.list();

        //Efetuar request a um peer no momento do run da app
        new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(args[1]);
                int port = 8888;
                byte[] yourBytes;

                /*try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(files);
                    out.flush();
                    yourBytes = bos.toByteArray();
                }*/

                // Aqui vamos apenas mandar um SYN para avisar o programa que queremos começar a troca
                // O packBuilder do tipo 0 vai definir um SYN
                PackBuilder pb =  new PackBuilder(0);
                yourBytes = pb.toBytes();

                DatagramPacket request = new DatagramPacket(yourBytes, yourBytes.length, ip, port);
                DatagramSocket socket = new DatagramSocket();

                System.out.println("A mandar o SYN");
                socket.send(request);
                System.out.println("Mandei o SYN");

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
                System.out.println("Ora fds: " + hostName);
                System.out.println("Listening on port: " + port);

                DatagramSocket serverSocket = new DatagramSocket(port);

                while(true) {
                    byte[] inBuffer = new byte[1500];
                    DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                    serverSocket.receive(inPacket);

                    /*ByteArrayInputStream bis = new ByteArrayInputStream(inPacket.getData());
                    Object o;
                    try (ObjectInput in = new ObjectInputStream(bis)) {
                        o = in.readObject();
                    }*/


                    // Não sei como fazer para garantir que eles ficam a falar na mesma socket
                    PackBuilder pb = new PackBuilder().fromBytes(inPacket.getData());
                    System.out.println("Received SYN if 0 ==" + pb.getPacote());

                    // String[] list = (String[]) o;
                    // List<String> filesToSend = new ArrayList<>();

		            /*System.out.println("A verificar diferenças");

                    assert files != null;
                    for (String f : files) {
			            for (String f1 : list) {
				            if(!f.equals(f1)) {
					            filesToSend.add(f);
				            }
			            }
                    }*/

                    ClientHandler ch = new ClientHandler(inPacket);
                    Thread t = new Thread(ch);
                    t.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
		System.out.println("ERRO!");
            }
        }).start();
    }
}
