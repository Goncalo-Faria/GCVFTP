package Transport.Unit;

import Estado.BitManipulator;

import java.nio.ByteBuffer;

public class ControlPacket extends Packet {

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

    private Type type; /* control message type*/
    private short extendedtype=0; /*para a aplicação*/
    private int timestamp=0; /*tempo desde que a ligação começou*/
    private byte[] information = new byte[0]; /* informação de controlo extra ao header*/
    private int ack = 0; /* números de sequência até este valor foram recebidos */
    private boolean readonly = false; /* é possivel alterar o pacote */
    private ByteBuffer b; /* util para ler o pacote de dados*/

    ControlPacket( byte[] data){
        this.readonly = true;
        BitManipulator extrator = new BitManipulator(data);
        this.type = Type.values()[extrator.flip().getShort()];
        this.extendedtype = extrator.getShort();
        this.timestamp = extrator.getInt();
        this.ack = extrator.getInt();
        this.information = new byte[data.length*8 - Packet.header_size * 8];
        ByteBuffer.wrap(data).get(this.information,0,data.length - Packet.header_size );
    }

    public ControlPacket( byte[] information, Type t, int timestamp){
        this(t,timestamp);
        this.information = information;
    }

    public ControlPacket( Type t, int timestamp){
        this.type = t;
        this.timestamp = timestamp;
    }

    public void setAck(int ack){ if(!this.readonly) this.ack = ack; }

    public void setExtendedType(short extendedtype){ if(!this.readonly) this.extendedtype = extendedtype; }

    public int getAck() { return this.ack; }

    public int getTimestamp() { return this.timestamp; }

    public ControlPacket.Type getType() { return this.type; }

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

        return  BitManipulator.allocate(Packet.header_size + this.information.length).
                flip().put((short)this.type.ordinal()).
                put(this.extendedtype).
                put(this.timestamp).
                put(this.ack).
                put(this.information).array();

    }

    @Override
    public boolean equals(Object obj) {
        if( !( obj instanceof ControlPacket ) )
            return false;

        ControlPacket cp = (ControlPacket)obj;

        boolean acc= true;

        int min = ( cp.getInformation().length < this.information.length) ?
                cp.getInformation().length:
                this.information.length;

        for(int i=0; i< min; i++)
            acc = acc && (cp.getInformation()[i] == this.information[i]);

        return acc &&
                (this.getType().equals(cp.getType())) &&
                (cp.getExtendedtype() == this.extendedtype) &&
                (cp.getAck() == this.ack) &&
                (cp.getTimestamp() == this.timestamp);

    }
}
