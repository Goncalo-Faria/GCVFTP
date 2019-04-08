package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class NOPE extends ControlPacket {

    public static int size = ControlPacket.header_size;

    public NOPE( BitManipulator extrator ) {
        super(ControlPacket.Type.NOPE, extrator.getShort()/*extended*/, extrator.getInt());
    }

    public int size(){
        return size;
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.array();

    }
}
