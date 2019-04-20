package Transport.ControlPacketTypes;

import Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class SUP extends ControlPacket {

    public static int size = ControlPacket.header_size ;

    public SUP( BitManipulator extrator ) {
        super(ControlPacket.Type.SUP, extrator.getShort()/*extended*/, extrator.getInt());
    }

    public int size(){
        return size;
    }

    public SUP(short extendedtype, int timestamp){
        super(ControlPacket.Type.SUP,extendedtype,timestamp);
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.array();

    }
}
