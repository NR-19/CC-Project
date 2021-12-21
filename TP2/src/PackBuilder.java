import java.io.*;

public class PackBuilder implements Serializable {

    private int pacote;
    private String filename;
    private int chunk;
    private int tamanho_fich;
    private byte[] data;

    //TIPOS DE PACOTE
    //  TIPO 1 - LISTA DE NOMES DOS FICHEIROS QUE POSSUI
    //  TIPO 2 - LISTA DE NOMES DOS FICHEIROS QUE PRECISA
    //  TIPO 3 - TRANSFERENCIA DE FICHEIROS
    //  TIPO 4 - ACK
    //  TIPO 5 - ERROR
    public static final int TIPO1 = 1;
    public static final int TIPO2 = 2;
    public static final int TIPO3 = 3;
    public static final int TIPO4 = 4;
    public static final int TIPO5 = 5;

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

    public byte[] getData() {
        return this.data;
    }

    public PackBuilder() {
        this.pacote = -1;
        this.filename = "";
        this.chunk = 0;
        this.tamanho_fich = 0;
    }

    public PackBuilder(int pacote, String filename, int chunk, int tamanho_fich, byte[] data){
        this.pacote = pacote;
        this.filename = filename;
        this.chunk = chunk;
        this.tamanho_fich = tamanho_fich;
        this.data = data;
    }

    public PackBuilder(PackBuilder pb){
        this.pacote = pb.getPacote();
        this.filename = pb.getFilename();
        this.chunk = pb.getChunk();
        this.tamanho_fich = pb.getTamanho_fich();
	this.data = pb.getData();
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

    // Passa um objeto para um array de bytes
    public static byte[] objectToData(Object o) {
        byte [] yourBytes = null;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            yourBytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return yourBytes;
    }

    public Object bytesToObject() {
        ByteArrayInputStream bis = new ByteArrayInputStream(this.data);
        Object o = null;

        try (ObjectInput in = new ObjectInputStream(bis)) {
            o = in.readObject();
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }

        return o;
    }

}
