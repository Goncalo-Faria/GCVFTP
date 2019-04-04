package Transport.Unit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
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

    ControlPacket( BitSet data, int size){
        this.readonly = true;
        this.type = this.extract_type(data);
        this.extendedtype = this.extract_extendedtype(data);
        this.timestamp = this.extract_timestamp(data);
        this.ack = this.extract_ack(data);
        if(data.length()>96)
            this.information = data.get(96, size).toByteArray();

    }

    public ControlPacket( byte[] information, Type t, int timestamp){
        this(t,timestamp);
        this.information = information;
    }

    public ControlPacket( Type t, int timestamp){
        this.type = (short)t.ordinal();
        this.timestamp = timestamp;
    }

    private short extract_type( BitSet data ){
        BitSet bs = data.get(0,16);

        bs.set(0,false);

        byte[] dat = bs.toByteArray();

        //System.out.println(dat.length);

        short ans = ByteBuffer.wrap(dat).getShort();

        //System.out.println("type : " + ans);

        return ans;
    }

    private short extract_extendedtype( BitSet data ){
       short ans = ByteBuffer.wrap(data.get(16,32).toByteArray()).getShort();

       //System.out.println("extended type : " + ans);

       return ans;
    }

    private int extract_timestamp( BitSet data ){
        int ans =  ByteBuffer.wrap(data.get(64,96).toByteArray()).getInt();

        //System.out.println("timestamp : " + ans);

        return ans;
    }

    private int extract_ack( BitSet data ){
        BitSet s = BitSet.valueOf(ByteBuffer.wrap(data.get(32,64).toByteArray()));
        s.set(0,false);

        int ans = ByteBuffer.wrap(s.toByteArray()).getInt();

        //System.out.println("ack : " + ans);

        return ans;
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

        ByteBuffer baseb = ByteBuffer.
                allocate(4);

        ByteBuffer viewb = ByteBuffer.wrap(baseb.array());

        viewb.putShort(this.type).
                putShort(this.extendedtype);


        BitSet bb = BitSet.valueOf( viewb.array() );

        bb.set(0,true);

        byte[] type = bb.toByteArray();

        ByteBuffer b = ByteBuffer.allocate(3 * 4 + this.information.length);

        byte[] answer = b.array();

        b.put(type).putInt(this.ack).putInt(this.timestamp).put(this.information);

        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if( !( obj instanceof ControlPacket ) )
            return false;

        ControlPacket cp = (ControlPacket)obj;

        boolean acc= true;

        int min = ( cp.getInformation().length < this.information.length) ? cp.getInformation().length: this.information.length;

        for(int i=0; i< min; i++)
            acc = acc && (cp.getInformation()[i] == this.information[i]);

        return acc && this.getType().equals(cp.getType()) &&
                (cp.getExtendedtype() == this.extendedtype) &&
                    (cp.getAck() == this.ack) && (cp.getTimestamp() == this.timestamp);

    }
}
