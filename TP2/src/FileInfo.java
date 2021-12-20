import java.io.File;
import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileInfo implements Serializable {

    public String nomeFicheiro;
    public long dataModificacao;

    public FileInfo(File file) throws IOException {
        this.nomeFicheiro = file.getName();
        this.dataModificacao = file.lastModified();
    }

    //passar tudo para string

    //passar de string para a class


    // Procurar as diferenças entre as listas
    public static List<String> neededToSend(List<FileInfo> minhas, List<FileInfo> dele) {
        List<String> result = new ArrayList<>();

        for (FileInfo f1 : dele) {
            boolean check = false;
            for (FileInfo f2 : minhas) {
                if (f1.nomeFicheiro.equals(f2.nomeFicheiro)) {
                    if (f1.dataModificacao <= f2.dataModificacao) {
                        check = true;
                        break;
                    }
                }
            }
            if (!check) {
                result.add(f1.nomeFicheiro);
            }
        }
        return result;
    }
}
