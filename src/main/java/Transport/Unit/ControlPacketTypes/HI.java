package Transport.Unit.ControlPacketTypes;

import Transport.Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class HI extends ControlPacket {

    public static int size = ControlPacket.header_size + 4*4;

    private final int maxpacket;
    private final int maxwindow;
    private final int seq;
    private final int timestamp;

    public HI(short extendedtype, int timestamp, int maxpacket, int maxwindow){
        super(ControlPacket.Type.HI,extendedtype);
        this.timestamp = timestamp;
        this.maxpacket=maxpacket;
        this.maxwindow=maxwindow;
        this.seq = (int)(Math.random()*1000)+1;
    }

    public HI( BitManipulator extrator ){
        super(ControlPacket.Type.HI, extrator.getShort()/*extended*/);
        this.timestamp = extrator.getInt();
        this.maxpacket = extrator.getInt();
        this.seq = extrator.getInt();
        this.maxwindow = extrator.getInt();
    }

    public byte[] extendedSerialize( BitManipulator extractor ){
        return  extractor.put(timestamp).put(this.maxpacket).put(this.seq).put(this.maxwindow).array();
    }

    public int getTimestamp(){return  this.timestamp; }

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
