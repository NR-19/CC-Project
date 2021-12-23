import java.io.*;

public abstract class LogBuilder {

    private LogBuilder(){
    }

    public static void createFile() {
        try {
            File logfile = new File("logs.txt");
            if (logfile.createNewFile()) {
                System.out.println("Ficheiro de logs criado: " + logfile.getName());
            } else {
                System.out.println("Ficheiro de logs encontrado.");
            }
        } catch (IOException e) {
            System.out.println("Ocorreu um erro na criação do ficheiro.");
            e.printStackTrace();
        }
    }

    public static void errorLine(String e) throws IOException {
        writeLine("ERROR: "+ e + "\n");
    }

    public static void writeLine(String text) throws IOException {
        FileWriter fileWriter = new FileWriter("logs.txt");
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(text+"\n");
        fileWriter.close();
        bufferedWriter.close();
    }

}
