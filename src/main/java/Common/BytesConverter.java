package Common;

import Model.Packet;

import java.io.*;

public class BytesConverter {
    public static byte[] toInputStream(Packet t) {
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(t);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Packet fromInputStream(InputStream inputStream) {
        Packet object = null;

        try {
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            object = (Packet) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }
}
