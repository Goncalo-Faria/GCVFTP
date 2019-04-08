package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class BYE extends ControlPacket {

    public BYE( BitManipulator extrator ) {
        super(ControlPacket.Type.BYE, extrator.getShort()/*extended*/, extrator.getInt());
    }
}
