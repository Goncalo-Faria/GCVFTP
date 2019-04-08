package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class NOPE extends ControlPacket {

    public NOPE( BitManipulator extrator ) {
        super(ControlPacket.Type.NOPE, extrator.getShort()/*extended*/, extrator.getInt());
    }
}
