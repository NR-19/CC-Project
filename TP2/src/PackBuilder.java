import java.io.*;

public class PackBuilder implements Serializable {

    private int pacote;
    private String filename;
    private int chunk;
    private int tamanho_fich;

    //TIPOS DE PACOTE
    //  TIPO 1 - LISTA DE NOMES DO FICHEIRO
    //  TIPO 2 - TRANSFERENCIA DE FICHEIROS
    //  TIPO 3 - ACK
    //  TIPO 4 - ERROR
    public static final int TIPO1 = 1;
    public static final int TIPO2 = 2;
    public static final int TIPO3 = 3;
    public static final int TIPO4 = 4;

    //DEFINE TAMANHO DOS BLOCOS - ?

    //1 THREAD PARA CADA FICHEIRO A SER ENVIADO


    public void send_filelistnames(){

    }

    public void recieve_filelistnames(){

    }

    public int getPacote() {
        return this.pacote;
    }

    public String getFilename() {
        return this.filename;
    }

    public int getChunk() {
        return this.chunk;
    }

    public int getTamanho_fich() {
        return this.tamanho_fich;
    }

    public PackBuilder() {
        this.pacote = -1;
        this.filename = "";
        this.chunk = 0;
        this.tamanho_fich = 0;
    }

    public PackBuilder(int pacote) {
        this.pacote = pacote;
        this.filename = "";
        this.chunk = 0;
        this.tamanho_fich = 0;
    }

    public PackBuilder(int pacote, String filename, int chunk, int tamanho_fich){
        this.pacote = pacote;
        this.filename = filename;
        this.chunk = chunk;
        this.tamanho_fich = tamanho_fich;
    }

    public PackBuilder(PackBuilder pb){
        this.pacote = pb.getPacote();
        this.filename = pb.getFilename();
        this.chunk = pb.getChunk();
        this.tamanho_fich = pb.getTamanho_fich();
    }

    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        }
    }

    public PackBuilder fromBytes(byte[] data){
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        Object o;
        PackBuilder pb = null;
        try (ObjectInput in = new ObjectInputStream(bis)) {
            o = in.readObject();
            pb = new PackBuilder((PackBuilder) o);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return pb;
    }





}
