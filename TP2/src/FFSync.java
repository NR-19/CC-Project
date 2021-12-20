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

        //Efetuar request a um peer no momento do run
        new Thread(() -> {
            try {
                InetAddress ip = InetAddress.getByName(args[1]);
                int port = 8888;
                byte[] yourBytes;

                List<FileInfo> fileInfos = new ArrayList<>();

                assert files != null;
                for (File f : files) {
                    fileInfos.add(new FileInfo(f));
                }

                try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(fileInfos);
                    out.flush();
                    yourBytes = bos.toByteArray();
                }

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
            int port = 8888;
            try {
                InetAddress host = InetAddress.getLocalHost();
                String hostName = host.getHostName();
                System.out.println(hostName);
                System.out.println("Listening on port: " + port);

                DatagramSocket serverSocket = new DatagramSocket(port);

                while(true) {
                    byte[] inBuffer = new byte[1500];
                    DatagramPacket inPacket = new DatagramPacket(inBuffer, inBuffer.length);
                    serverSocket.receive(inPacket);

                    PackBuilder pb = new PackBuilder().fromBytes(inPacket.getData());
		
		    
		    System.out.println(pb.getData().length);

                    int pacote = pb.getPacote();
                    if (pacote == PackBuilder.TIPO1) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(pb.getData());
                        Object o;
                        try (ObjectInput in = new ObjectInputStream(bis)) {
                            o = in.readObject();
                        }
                        @SuppressWarnings("unchecked") List<FileInfo> fileInfos = (List<FileInfo>) o;

                        for(FileInfo f : fileInfos) {
                            System.out.println(f.dataModificacao + " - " + f.nomeFicheiro);
                        }
                    }

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

                    // Aqui podemos passar a porta a usar de maneira a manter uma comunicação
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
