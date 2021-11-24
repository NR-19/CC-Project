package UDP;

public class ClienteMain {
    public static void main(String[] args) {
        ClienteUDP2 cliente = new ClienteUDP2();
        String r = cliente.sendEcho("Hello World!");
        System.out.println("Received: " + r);

        cliente.sendEcho("end");
        cliente.close();
    }
}
