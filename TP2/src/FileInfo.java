import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class FileInfo {

    public String nomeFicheiro;
    public long dataModificacao;


    /*public FileInfo(Path path) throws IOException {
        BasicFileAttributes ficheiro = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
        this.nomeFicheiro = path.getFileName().toString();
        this.dataModificacao = ficheiro.lastModifiedTime().toMillis();
    }*/

    public FileInfo(File file) throws IOException {
        this.nomeFicheiro = file.getName();
        this.dataModificacao = file.lastModified();
    }

    //passar tudo para string

    //passar de string para a class

}
