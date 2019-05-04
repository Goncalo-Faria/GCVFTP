package Transport.Unit;

import Transport.Common.BitManipulator;

import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.zip.Adler32;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public abstract class Packet {

    public static Packet parse(byte[] udp_data) throws StreamCorruptedException {

        long checksum = ByteBuffer.wrap(udp_data).getLong();


        Checksum checks = new Adler32();
        checks.reset();
        checks.update(udp_data,8, udp_data.length-8);

        if( checks.getValue() == checksum ){
            boolean type = BitManipulator.msb(udp_data,8);

            if(type){
                //System.out.println("ctrl : ");
                return ControlPacket.parseControl(udp_data, 8);
            }else{
                //System.out.println("data : ");
                return new DataPacket(udp_data, 8);
            }
        }else{
            throw new StreamCorruptedException();
        }

    }

    abstract byte[] serialize(BitManipulator buffer);

    protected abstract int size();

    private byte[] serialized = null;

    private void setChecksum(){
        Checksum checks = new Adler32();
        checks.reset();

        BitManipulator buffer = new BitManipulator(new byte[this.size()]).skip(8);

        byte[] data = this.serialize(buffer);

        checks.update(data, 8, this.size()-8);

        this.serialized = ByteBuffer.wrap(data).putLong(checks.getValue()).array();

    }

    public byte[] markedSerialize(){
        if( this.serialized == null)
            this.setChecksum();

        return this.serialized;
    }

}
