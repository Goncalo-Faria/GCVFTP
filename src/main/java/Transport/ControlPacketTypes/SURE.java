package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class SURE extends ControlPacket {

    public static int size = ControlPacket.header_size;

    public SURE( BitManipulator extrator ) {
        super(ControlPacket.Type.SURE, extrator.getShort()/*extended*/, extrator.getInt());
    }

    public int size(){
        return size;
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.put(0).array();

    }
}
