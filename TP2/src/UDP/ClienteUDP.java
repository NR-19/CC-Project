package UDP;

import java.io.IOException;
import java.net.*;

public class ClienteUDP {
    private DatagramSocket socket;
    private InetAddress address;

    public ClienteUDP() {
        try {
            socket = new DatagramSocket();
            address = InetAddress.getByName("localhost");
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public String sendEcho(String msg) {
        String received = "";
        try {
            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 80);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            received = new String(packet.getData(), 0, packet.getLength());
            System.out.println(received);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return received;
    }

    public void close() {
        socket.close();
    }

    public static void main(String[] args) {
        ClienteUDP cliente = new ClienteUDP();
        cliente.sendEcho("Hello World");

        cliente.sendEcho("end");
        cliente.close();
    }
}
