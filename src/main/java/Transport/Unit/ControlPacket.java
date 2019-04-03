package Transport.Unit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class ControlPacket extends Packet {

    public static int header_size = 12;

    public enum Type{
        HI, /*syn*/
        OK, /*ack*/
        SURE, /*ack2*/
        NOPE, /*nack*/
        BYE, /*fin*/
        SUP, /*keepalive*/
        FORGETIT /*message drop*/
    }

    public static ControlPacket hi(int timestamp){ return new ControlPacket(Type.HI,timestamp); }

    public static ControlPacket ok(int timestamp){ return new ControlPacket(Type.OK,timestamp); }

    public static ControlPacket sure(int timestamp){ return new ControlPacket(Type.SURE,timestamp); }

    public static ControlPacket nope(int timestamp){ return new ControlPacket(Type.NOPE,timestamp); }

    public static ControlPacket sup(int timestamp){ return new ControlPacket(Type.SUP,timestamp); }

    public static ControlPacket bye(int timestamp){ return new ControlPacket(Type.BYE,timestamp); }

    public static ControlPacket forgetit(int timestamp){ return new ControlPacket(Type.FORGETIT,timestamp); }

    private short type; /* control message type*/
    private short extendedtype=0; /*para a aplicação*/
    private int timestamp=0; /*tempo desde que a ligação começou*/
    private byte[] information = new byte[0]; /* informação de controlo extra ao header*/
    private int ack = 0; /* números de sequência até este valor foram recebidos */
    private boolean readonly = false; /* é possivel alterar o pacote */
    private ByteBuffer b; /* util para ler o pacote de dados*/

    ControlPacket( BitSet data ){
        this.readonly = true;
        this.type = this.extract_type(data);
        this.extendedtype = this.extract_extendedtype(data);
        this.timestamp = this.extract_timestamp(data);
        this.ack = this.extract_ack(data);
        if(data.length()>96)
            this.information = data.get(96, data.length()).toByteArray();

    }

    public ControlPacket( byte[] information, Type t, int timestamp){
        this(t,timestamp);
        this.information = information;
    }

    public ControlPacket( Type t, int timestamp){
        this.type = (short)t.ordinal();
    }

    private short extract_type( BitSet data ){
        return ByteBuffer.wrap(data.get(1,16).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private short extract_extendedtype( BitSet data ){
        return ByteBuffer.wrap(data.get(16,32).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private int extract_timestamp( BitSet data ){
        return ByteBuffer.wrap(data.get(64,96).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private int extract_ack( BitSet data ){
        return ByteBuffer.wrap(data.get(33,64).toByteArray()).
                order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public void setAck(int ack){ if(!this.readonly) this.ack = ack; }

    public void setExtendedType(short extendedtype){ if(!this.readonly) this.extendedtype = extendedtype; }

    public int getAck() { return this.ack; }

    public int getTimestamp() { return this.timestamp; }

    public ControlPacket.Type getType() { return Type.values()[this.type]; }

    public byte[] getInformation() { return this.information; }

    public short getExtendedtype() { return this.extendedtype; }

    public void startBuffer(){
        this.b = ByteBuffer.wrap(this.information);
    }

    public int getInt(){
        return this.b.getInt();
    }

    public String asString(){
        return this.b.asCharBuffer().toString();
    }

    public byte[] serialize(){
        int type_mask = (2^15 + (int)this.type)*2^16 + (int)this.extendedtype;
        ByteBuffer b = ByteBuffer.allocate(3 * 32 + this.information.length);
        b.putInt(type_mask).putInt(this.ack).putInt(this.timestamp).put(this.information);
        return b.array();
    }

}
