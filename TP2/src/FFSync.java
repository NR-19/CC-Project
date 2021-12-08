import java.io.File;

public class FFSync {
    public static void main(String[] args) {
        File logs = new File("logs.txt");

        for (String arg : args) {
            System.out.println(arg);
        }
    }
}
