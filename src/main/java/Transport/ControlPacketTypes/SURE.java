package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class SURE extends ControlPacket {

    public static int size = ControlPacket.header_size;

    public static short ack_hi = 239;

    public SURE( BitManipulator extrator ) {
        super(ControlPacket.Type.SURE, extrator.getShort()/*extended*/, extrator.getInt());
    }

    public int size(){
        return size;
    }

    public SURE(short extendedtype, int timestamp){
        super(ControlPacket.Type.SURE,extendedtype,timestamp);
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.array();

    }
}
