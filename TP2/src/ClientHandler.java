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
        System.out.println("Packet Received from: " + inPacket.getAddress().toString() + ": " + inPacket.getPort());
        this.socket = new DatagramSocket();
    }

    @Override
    public void run() {
        PackBuilder pb = new PackBuilder().fromBytes(this.inPacket.getData());
        byte[] inBuffer = this.inPacket.getData();                  // get client Data
        InetAddress clientIp = this.inPacket.getAddress();          // get client IP
        int port = this.inPacket.getPort();                         // get client port

        String receivedString = new String(inBuffer);

        // Aqui vamos ter um ciclo "infinito" que vai enviar e ficar à espera da resposta
        // O ciclo acaba quando for para acabar a conexão

        // Isto vai ter de estar dentro de um if else que trabalha conforme o que recebe
        // Vamos mandar um ACK só para teste, o objetivo é enviar os files
        PackBuilder packSend = new PackBuilder(2);
        byte[] outBuffer = new byte[0];
        try {
            outBuffer = packSend.toBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Tem de haver maneira de manter a conexão na mesma port

        // create outgoing packet with data, client IP and client port
        DatagramPacket outPacket = new DatagramPacket(outBuffer, outBuffer.length, clientIp, port);
        try {
            this.socket.send(outPacket);                            // send packet
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
