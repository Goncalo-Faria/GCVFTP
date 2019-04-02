package Transport.Unit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class DataPacket extends Packet {

    public enum Flag{
        MIDDLE,
        LAST,
        FIRST,
        SOLO,
    }

    private int timestamp=0;
    private int seq=0;
    private int window=0;
    private byte[] information = new byte[0];
    private short flag=0;


    DataPacket( BitSet data ){
        this.timestamp = this.extract_timestamp(data);
        this.seq = this.extract_seq(data);
        this.window = this.extract_window(data);
        this.information = data.get(96, data.length()).toByteArray();
        this.flag = this.extract_flag(data);

    }

    public DataPacket(byte[] data, int timestamp, int seq, int window, DataPacket.Flag flag){
        this.information = data;
        this.timestamp = timestamp;
        this.seq = seq;
        this.window = window;
        this.flag = (short)flag.ordinal();
    }

    private int extract_timestamp( BitSet data ){
        return ByteBuffer.wrap(data.get(32,64).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private int extract_seq( BitSet data ){
        return ByteBuffer.wrap(data.get(1,32).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private int extract_window( BitSet data){
        return ByteBuffer.wrap(data.get(66,96).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private short extract_flag( BitSet data){
        return ByteBuffer.wrap(data.get(64,66).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public byte[] serialize(){

        ByteBuffer b = ByteBuffer.allocate(3 * 32 + this.information.length);
        b.putInt(this.seq).putInt(this.timestamp).putShort(this.flag).putInt(this.window).put(this.information);
        return b.array();

    }
}
