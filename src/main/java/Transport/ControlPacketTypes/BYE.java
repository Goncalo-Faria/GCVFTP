package Transport.ControlPacketTypes;

import Common.BitManipulator;
import Transport.Unit.ControlPacket;

public class BYE extends ControlPacket {

    public static int size = ControlPacket.header_size ;

    public BYE( BitManipulator extrator ) {
        super(ControlPacket.Type.BYE, extrator.getShort()/*extended*/, extrator.getInt());
    }

    public BYE(short extendedtype, int timestamp){
        super(ControlPacket.Type.BYE,extendedtype,timestamp);
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.array();

    }

    public int size(){ return size; }

}
