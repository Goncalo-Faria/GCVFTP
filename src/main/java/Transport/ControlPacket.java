package Transport;

import java.nio.ByteBuffer;

public class ControlPacket extends Packet {

    public static int hi = 0;
    public static int ack = 1;
    public static int ack2 = 2;
    public static int nack = 3 ;

    private int type ;//(31 bytes)

    private ByteBuffer bb;

    public ControlPacket(byte[] data){
        this.bb = ByteBuffer.allocate(data.length);
    }

    public void synack(int port){
        byte[] packet = new byte[2 * Integer.BYTES + 1];

        packet[0] = 1;


    }
}
