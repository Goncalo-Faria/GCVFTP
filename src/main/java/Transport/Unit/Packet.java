package Transport.Unit;

import Common.BitManipulator;

public abstract class Packet {

    public static Packet parse(byte[] udp_data){

        //System.out.println("got " + udp_data.length + " bytes ");

        boolean type = BitManipulator.msb(udp_data,0);

        if(type){
            //System.out.println("ctrl : ");
            return ControlPacket.parseControl(udp_data);
        }else{
            //System.out.println("data : ");
            return new DataPacket(udp_data);
        }
    }


    public abstract byte[] serialize();

}
