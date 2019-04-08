package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class FORGETIT extends ControlPacket {

    public FORGETIT( BitManipulator extrator ) {
        super(ControlPacket.Type.FORGETIT, extrator.getShort()/*extended*/, extrator.getInt()/*timestamp*/);
    }
}
