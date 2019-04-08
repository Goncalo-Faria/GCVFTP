package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class OK extends ControlPacket {

    public static int size = ControlPacket.header_size + 4 ;

    private int ack;

    public OK( BitManipulator extrator ) {
        super(ControlPacket.Type.OK, extrator.getShort()/*extended*/, extrator.getInt());
        this.ack = extrator.getInt();
    }
    public int size(){
        return size;
    }

    public byte[] extendedSerialize( BitManipulator extractor ){

        return  extractor.put(ack).array();

    }
}
