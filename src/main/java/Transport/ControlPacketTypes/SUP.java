package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class SUP extends ControlPacket {

    public SUP( BitManipulator extrator ) {
        super(ControlPacket.Type.SUP, extrator.getShort()/*extended*/, extrator.getInt());
    }
}
