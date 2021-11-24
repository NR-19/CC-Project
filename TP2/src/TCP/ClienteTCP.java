package TCP;

import java.io.*;
import java.net.*;

public class ClienteTCP {
    public static void main(String[] args) {

        try {
            Socket clientSocket = new Socket("10.2.2.1", 80);

            DataOutputStream infoParaServidor = new DataOutputStream(clientSocket.getOutputStream());
            BufferedReader infoDoServidor = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            infoParaServidor.writeBytes("Hello Server!");
            String mensagem_para_cliente = infoDoServidor.readLine();

            clientSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
