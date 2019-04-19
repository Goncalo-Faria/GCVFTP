package Transport.ControlPacketTypes;

import Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class OK extends ControlPacket {

    public static int size = ControlPacket.header_size + 4 ;

    private int ack;

    public OK( BitManipulator extrator ) {
        super(ControlPacket.Type.OK, extrator.getShort()/*extended*/, extrator.getInt());
        this.ack = extrator.getInt();
    }

    public OK(short extendedtype, int timestamp, int ack){
        super(ControlPacket.Type.OK,extendedtype,timestamp);
        this.ack = ack;
    }

    public int size(){
        return size;
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.put(ack).array();

    }
}
