package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;

public class HI extends ControlPacket {

    public static int size = ControlPacket.header_size + 4*3;

    private int maxpacket;
    private int maxwindow;
    private int seq;

    public HI( short extendedtype, int timestamp, int maxpacket, int maxwindow){
        super(ControlPacket.Type.HI,extendedtype,timestamp);
        this.maxpacket=maxpacket;
        this.maxwindow=maxwindow;
        this.seq= (int)(Math.random()*Integer.MAX_VALUE);
    }

    public HI( BitManipulator extrator ){
        super(ControlPacket.Type.HI, extrator.getShort()/*extended*/, extrator.getInt());
        this.maxpacket = extrator.getInt();
        this.seq = extrator.getInt();
        this.maxwindow = extrator.getInt();
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.put(this.maxpacket).put(this.seq).put(this.maxwindow).array();

    }

    public int getMTU(){
        return this.maxpacket;
    }

    public int getMaxWindow(){
        return this.maxwindow;
    }

    public int getSeq(){
        return this.seq;
    }

    public int size(){
        return size;
    }
}
