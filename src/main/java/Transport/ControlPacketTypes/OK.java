package Transport.ControlPacketTypes;

import Estado.BitManipulator;
import Transport.Unit.ControlPacket;

public class OK extends ControlPacket {

    private int ack;

    public OK( BitManipulator extrator ) {
        super(ControlPacket.Type.OK, extrator.getShort()/*extended*/, extrator.getInt());
        this.ack = extrator.getInt();
    }
}
