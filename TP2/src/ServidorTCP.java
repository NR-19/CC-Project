import java.io.*;
import java.net.*;

public class ServidorTCP {

    public static void main(String[] args) {


        ServerSocket welcomeSocket;
        try {
            welcomeSocket = new ServerSocket(80);
            while(true){
                Socket connectionSocket = welcomeSocket.accept();

                BufferedReader infoDoCliente = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
                String mensagemDoCliente = infoDoCliente.readLine();

                DataOutputStream infoParaCliente = new DataOutputStream(connectionSocket.getOutputStream());
                infoParaCliente.writeBytes("Hello Client!");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
