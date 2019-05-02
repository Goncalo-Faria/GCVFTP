package Test;

import Transport.Unit.ControlPacketTypes.*;
import Transport.Receiver.IntervalPacket;
import Transport.Receiver.SimpleSeqChain;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

public class GCVFTP {
    public static void main(String[] args) {


        /*ByteBuffer base = ByteBuffer.allocate(4);
        base.putInt(-1);

        System.out.println( BitManipulator.msb( base.array(),0) );
        */
        ControlPacket cp = new OK((short)422,42,445,42,42,42);

        System.out.println(cp.getType());

        Packet p =  Packet.parse(cp.serialize());

        if( p instanceof ControlPacket) {
            ControlPacket c = (ControlPacket) p;
            System.out.println("functionally works");
            System.out.println(c.equals(cp));
        }else{
            System.out.println(" it's data ");
        }

        DataPacket dp = new DataPacket( "ola".getBytes() ,"ola".getBytes().length,8448,DataPacket.Flag.SOLO);

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


        System.out.println("examinar checks");

        SimpleSeqChain l = new SimpleSeqChain(30);

        for(int i = 1; i< 22; i = i + 2) {
            DataPacket packet = new DataPacket(new byte[20], 0, i, DataPacket.Flag.SOLO);

            l.add( packet );

        }

        System.out.println(" max seq " + l.maxSeq());

        System.out.println(" min seq " + l.minSeq());


        DataPacket packet = new DataPacket(new byte[20], 0, 2, DataPacket.Flag.SOLO);

        l.add( packet );

        packet = new DataPacket(new byte[20], 0, 4, DataPacket.Flag.SOLO);

        l.add( packet );

        IntervalPacket tmp = l.take();

        System.out.println(tmp.min() + " - " + tmp.max());

        for(DataPacket pac : tmp.getpackets())
            System.out.println(pac.getSeq());

    }
}
