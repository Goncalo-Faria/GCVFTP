package Transport.ControlPacketTypes;

import Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class NOPE extends ControlPacket {

    public static int size = ControlPacket.header_size;

    public NOPE( BitManipulator extrator ) {
        super(ControlPacket.Type.NOPE, extrator.getShort()/*extended*/, extrator.getInt());
    }

    public int size(){
        return size;
    }

    public NOPE(short extendedtype, int timestamp){
        super(ControlPacket.Type.NOPE,extendedtype,timestamp);
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.array();

    }
}
