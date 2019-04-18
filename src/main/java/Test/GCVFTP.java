package Test;

import Transport.ControlPacketTypes.*;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

public class GCVFTP {
    public static void main(String[] args) {


        /*ByteBuffer base = ByteBuffer.allocate(4);
        base.putInt(-1);

        System.out.println( BitManipulator.msb( base.array(),0) );
        */
        ControlPacket cp = new OK((short)422,42,445);

        System.out.println(cp.getType());

        Packet p =  Packet.parse(cp.serialize());

        if( p instanceof ControlPacket) {
            ControlPacket c = (ControlPacket) p;
            System.out.println("functionally works");
            System.out.println(c.equals(cp));
        }else{
            System.out.println(" it's data ");
        }

        DataPacket dp = new DataPacket( "ola".getBytes() ,32,884,8448,DataPacket.Flag.FIRST);

        p =  Packet.parse(dp.serialize());

        if( p instanceof DataPacket) {
            DataPacket c = (DataPacket) p;

            System.out.println(((DataPacket) p).getFlag());

            System.out.println(new String(c.getData()));
            System.out.println("functionally works");
            System.out.println(c.equals(dp));
        }else{
            System.out.println(" it's control ");
        }


    }
}
