package Transport.Unit;

import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class Packet {

    public static Packet parse(byte[] udp_data){

        System.out.println("got " + udp_data.length + " bytes ");

        BitSet message = BitSet.valueOf(udp_data);

        boolean type = message.get(0);

        if(type){
            System.out.print("control : \\ ");
            return new ControlPacket(message, udp_data.length);
        }else{
            System.out.print("data : ");
            return new DataPacket(message, udp_data.length);
        }
    }

    public abstract byte[] serialize();

}
