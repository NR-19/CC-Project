import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class ClientHandler implements Runnable{

    private DatagramPacket inPacket;
    private DatagramSocket socket;

    public ClientHandler (DatagramPacket inPacket) throws SocketException {
        this.inPacket = inPacket;
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        PackBuilder pb = new PackBuilder().fromBytes(this.inPacket.getData());
        InetAddress clientIp = this.inPacket.getAddress();          // get client IP
        int port = this.inPacket.getPort();                         // get client port
        byte[] outBuffer = new byte[0];

        // Aqui vamos ter um ciclo "infinito" que vai enviar e ficar à espera da resposta
        // O ciclo acaba quando for para acabar a conexão
        while(true) {
            // Isto vai ter de estar dentro de um if else que trabalha conforme o que recebe
            // Vamos mandar um ACK só para teste, o objetivo é enviar os files
            /*if (pb.getPacote() == 0) {
                // PackBuilder packSend = new PackBuilder(2);
                try {
                    outBuffer = packSend.toBytes();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Mandei");
            } else {
                System.out.println("Olá");
            }*/

            // create outgoing packet with data, client IP and client port
            DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, clientIp, port);
            try {
                this.socket.send(outPacket);                            // send packet
                // this.socket.close();
                this.socket.receive(this.inPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
