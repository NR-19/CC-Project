import java.io.*;

public class PackBuilder {

    private int pacote;
    private String filename;
    private int chunk;
    private int tamanho_fich;


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

    public byte[] toBytes() throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(this);
            out.flush();
            return bos.toByteArray();
        }
    }



}
