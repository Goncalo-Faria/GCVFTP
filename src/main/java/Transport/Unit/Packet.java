package Transport.Unit;


import java.util.BitSet;

public abstract class Packet {

    public static Packet parse(byte[] udp_data){
        BitSet message = BitSet.valueOf(udp_data);
        boolean type = message.get(0);
        if(type){
            return new ControlPacket(message);
        }else{
            return new DataPacket(message);
        }
    }

    public abstract byte[] serialize();

}
