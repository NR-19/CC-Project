import java.io.IOException;
import java.net.*;

public class ClienteUDP {
    private static InetAddress IPAdress;

    public static void main(String[] args) {

        try {
            DatagramSocket clientSocket = new DatagramSocket();

            InetAddress IPAddress = InetAddress.getByName("10.2.2.1");

            byte[] sendData = new byte[1024];
            byte[] receiveData = new byte[1024];

            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAdress, 9876);

            clientSocket.send(sendPacket);

            DatagramPacket receivePacket = null;
            clientSocket.receive(receivePacket);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
