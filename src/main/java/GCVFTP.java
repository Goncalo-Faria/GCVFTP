import Estado.BitManipulator;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

public class GCVFTP {
    public static void main(String[] args) {


        /*ByteBuffer base = ByteBuffer.allocate(4);
        base.putInt(-1);

        System.out.println( BitManipulator.msb( base.array(),0) );
        */

        ControlPacket cp = new ControlPacket( new byte[40], ControlPacket.Type.SURE ,4422);
        cp.setAck(44);
        cp.setExtendedType((short)17);

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

            System.out.println(new String(c.getData()));
            System.out.println("functionally works");
            System.out.println(c.equals(dp));
        }else{
            System.out.println(" it's control ");
        }


    }
}
