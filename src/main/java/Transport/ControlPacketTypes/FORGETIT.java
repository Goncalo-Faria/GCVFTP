package Transport.ControlPacketTypes;

import Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class FORGETIT extends ControlPacket {

    public static int size = ControlPacket.header_size;

    public FORGETIT( BitManipulator extrator ) {
        super(Type.FORGETIT, extrator.getShort()/*extended*/, extrator.getInt()/*timestamp*/);
    }

    public FORGETIT(short extendedtype, int timestamp){
        super(Type.FORGETIT,extendedtype,timestamp);
    }

    public int size(){
        return size;
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.array();

    }
}
