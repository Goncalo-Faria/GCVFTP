package Transport.Unit.ControlPacketTypes;

import Transport.Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class HI extends ControlPacket {

    public static int size = ControlPacket.header_size + 4*3;

    private final int maxpacket;
    private final int maxwindow;
    private final int seq;

    public HI(short extendedtype, int maxpacket, int maxwindow){
        super(ControlPacket.Type.HI,extendedtype);

        this.maxpacket=maxpacket;
        this.maxwindow=maxwindow;
        this.seq = (int)(Math.random()*1000)+1;
    }

    public HI( BitManipulator extrator ){
        super(ControlPacket.Type.HI, extrator.getShort()/*extended*/);
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

    protected int size(){
        return size;
    }
}
