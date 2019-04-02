package Transport.Unit;

import java.nio.ByteBuffer;
import java.util.BitSet;

public class ControlPacket extends Packet {

    public static int hi = 0; /*syn*/
    public static int ok = 1; /*ack*/
    public static int sure = 2; /*ack2*/
    public static int nope = 3; /*nack*/
    public static int bye = 4; /*fin*/
    public static int sup = 5; /*keepalive*/
    public static int shit = 6; /*message drop*/

    private BitSet type;
    private BitSet extendedtype; /*para a aplicação*/
    private BitSet timestamp; /*calcular rrt*/
    private BitSet information;

    private ByteBuffer bb;

    public ControlPacket(byte[] data){
        this.bb = ByteBuffer.allocate(data.length);
    }

    ControlPacket( BitSet data ){
        this.type = data.get(1,16);
        this.extendedtype = data.get(16,32);
        this.timestamp = data.get(64, 96);
        if(data.length()>96){
            this.information = data.get(96, data.length());
        }

    }

    public void synack(int port){
        byte[] packet = new byte[2 * Integer.BYTES + 1];

        packet[0] = 1;

    }
}
