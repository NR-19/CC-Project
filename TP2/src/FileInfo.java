import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;

public class FileInfo {

    public String nomeFicheiro;
    public long DataCriacao; //possivelmete retirar pq n é necessário
    public long DataModificacao;


    public FileInfo(Path path) throws IOException {

        BasicFileAttributes ficheiro = Files.getFileAttributeView(path, BasicFileAttributeView.class).readAttributes();
        this.nomeFicheiro = path.getFileName().toString();
        this.DataCriacao = ficheiro.creationTime().toMillis();
        this.DataModificacao = ficheiro.lastModifiedTime().toMillis();
    }

    //passar tudo para string

    //passar de string para a class

}
