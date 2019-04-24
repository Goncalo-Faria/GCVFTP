package Transport.ControlPacketTypes;

import Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class OK extends ControlPacket {

    public static int size = ControlPacket.header_size + 4 * 4 ;

    private int seq;
    private int window=2;
    private int rtt;
    private int rttvar;

    public OK( BitManipulator extrator ) {
        super(ControlPacket.Type.OK, extrator.getShort()/*extended*/, extrator.getInt());
        this.seq = extrator.getInt();
        this.window = extrator.getInt();
        this.rtt = extrator.getInt();
        this.rttvar = extrator.getInt();
    }

    public OK(short extendedtype, int timestamp, int seq, int freebuffer, int rtt, int rttvar){
        super(ControlPacket.Type.OK,extendedtype,timestamp);
        this.seq = seq;
        this.rtt = rtt;
        this.rttvar = rttvar;
        this.window = (freebuffer > 2) ? freebuffer: 2;
    }

    public int getSeq(){
        return this.seq;
    }

    public int getRtt() { return this.rtt;}

    public int getRttVar() { return this.rttvar;}

    public int getWindow(){ return this.window;}

    public int size(){
        return size;
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.put(seq).put(window).put(rtt).put(rttvar).array();

    }
}
