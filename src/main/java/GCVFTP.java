
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

public class GCVFTP {
    public static void main(String[] args) {


        ControlPacket cp = new ControlPacket( ControlPacket.Type.BYE ,78000);
        cp.setAck(44);
        cp.setExtendedType((short)33);

        Packet p =  Packet.parse(cp.serialize());

        if( p instanceof ControlPacket) {
            ControlPacket c = (ControlPacket) p;
            System.out.println("functionally works");
            System.out.println(c.equals(cp));
        }else{
            System.out.println(" it's data ");
        }

        DataPacket dp = new DataPacket( new byte[10],32,64,88,DataPacket.Flag.SOLO);

        p =  Packet.parse(dp.serialize());

        if( p instanceof DataPacket) {
            DataPacket c = (DataPacket) p;
            System.out.println("functionally works");
            System.out.println(c.equals(dp));
        }else{
            System.out.println(" it's control ");
        }

    }
}
