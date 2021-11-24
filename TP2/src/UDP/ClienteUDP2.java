package UDP;

import java.io.IOException;
import java.net.*;

public class ClienteUDP2 {
    private DatagramSocket socket;
    private InetAddress address;

    private byte[] buf;

    public ClienteUDP2() {
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
            buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            received = new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return received;
    }

    public void close() {
        socket.close();
    }
}
