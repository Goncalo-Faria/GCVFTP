package Transport.Unit;

import Transport.Common.BitManipulator;
import Transport.Unit.ControlPacketTypes.*;

public abstract class ControlPacket extends Packet {

    public enum Type{
        HI, /*syn*/
        OK, /*ack*/
        SURE, /*ack2*/
        NOPE, /*shouldSendNope*/
        BYE, /*fin*/
        SUP, /*keepalive*/
        FORGETIT /*message drop*/
    }

    public static int header_size = 8;

    public static ControlPacket parseControl(byte[] data){
        BitManipulator extractor = new BitManipulator(data);
        Type ctype = Type.values()[extractor.flip().getShort()];

        switch(ctype){
            case HI:return new HI(extractor);
            case OK:return new OK(extractor);
            case SURE:return new SURE(extractor);
            case BYE:return new BYE(extractor);
            case SUP:return new SUP(extractor);
            case FORGETIT:return new FORGETIT(extractor);
            case NOPE:return new NOPE(extractor);
        }

        return null;
    }

    private Type type; /* control message type*/
    private short extendedtype=0; /*para a aplicação*/
    private int timestamp=0; /*tempo desde que a ligação começou*/
    
    protected ControlPacket(Type t, short extendedtype, int timestamp){
        this.type = t;
        this.timestamp = timestamp;
        this.extendedtype=extendedtype;
    }

    public void setExtendedType(short extendedtype){ this.extendedtype = extendedtype; }

    public int getTimestamp() { return this.timestamp; }

    public ControlPacket.Type getType() { return this.type; }

    public short getExtendedtype() { return this.extendedtype; }

    public byte[] serialize(){
        return  this.extendedSerialize(
                BitManipulator.allocate(this.size()).
                flip().put((short)this.type.ordinal()).
                put(this.extendedtype).
                put(this.timestamp));

    }

    protected abstract byte[] extendedSerialize(BitManipulator extractor);

    protected abstract int size();

    @Override
    public boolean equals(Object obj) {
        if( !( obj instanceof ControlPacket ) )
            return false;

        ControlPacket cp = (ControlPacket)obj;

        boolean acc= true;

        return (this.getType().equals(cp.getType())) &&
                (cp.getExtendedtype() == this.extendedtype) &&
                (cp.getTimestamp() == this.timestamp);

    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x \n");
        sb.append("type " + this.getType());
        sb.append("extcode " + this.getExtendedtype());
        sb.append("timestamp " + this.getTimestamp());
        return sb.toString();
    }
}
