package UDP;

public class ClienteMain {
    public static void main(String[] args) {
        ClienteUDP cliente = new ClienteUDP();
        String r = cliente.sendEcho("Hello World!");
        System.out.println("Received: " + r);

        r = cliente.sendEcho("end");
        System.out.println("Received: " + r);
        cliente.close();
    }
}
