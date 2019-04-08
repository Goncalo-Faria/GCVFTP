package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class SURE extends ControlPacket {

    public SURE( BitManipulator extrator ) {
        super(ControlPacket.Type.SURE, extrator.getShort()/*extended*/, extrator.getInt());
    }
}
