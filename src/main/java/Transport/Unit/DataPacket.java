package Transport.Unit;

import Estado.BitManipulator;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class DataPacket extends Packet {

    public enum Flag{
        MIDDLE,
        LAST,
        FIRST,
        SOLO;

        boolean[] mark( Flag s){

            boolean[] b = new boolean[2];

            switch( s ){
                case MIDDLE:
                    b[0] = false; b[1] = false;
                    break;
                case LAST:
                    b[0] = false; b[1] = true;
                    break;
                case FIRST:
                    b[0] = true; b[1] = false;
                    break;
                case SOLO:
                    b[0] = true; b[1] = true;
                    break;
            }

            return b;
        }

        Flag parse(boolean[] b){

            int sum=0;

            if ( b[0] ){
                sum+=2;
            }

            if( b[1] ){
                sum++;
            }
            return Flag.values()[sum];
        }

    }

    public static int header_size = 12;

    private int timestamp=0;
    private int seq=0;
    private int window=0;
    private byte[] information = new byte[0];
    private Flag flag;


    DataPacket( byte[] data ){

        BitManipulator extrator = new BitManipulator(data);

        this.seq = extrator.getInt();
        this.timestamp = extrator.getInt();
        this.flag = Flag.SOLO.parse(BitManipulator.msb2(data, 8));
        this.window = extrator.flip2().getInt();

        this.information = new byte[data.length - DataPacket.header_size ];

        ByteBuffer.wrap(data,DataPacket.header_size,data.length - DataPacket.header_size).
                get(this.information,
                0,
                data.length  - DataPacket.header_size );
    }

    public DataPacket(byte[] data, int timestamp, int seq, int window, DataPacket.Flag flag){
        this.information = data;
        this.timestamp = timestamp;
        this.seq = seq;
        this.window = window;
        this.flag = flag;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public byte[] getData() {
        return information;
    }

    public int getSeq() {
        return seq;
    }

    public int getWindow() {
        return window;
    }

    public DataPacket.Flag getFlag() {
        return this.flag;
    }

    public byte[] serialize(){

        return BitManipulator.allocate(DataPacket.header_size + this.information.length).
                put(this.seq).
                put(this.timestamp).
                put(this.window, this.flag.mark(this.flag)).
                put(this.information).
                array();

    }

    @Override
    public boolean equals(Object obj) {
        if( !( obj instanceof DataPacket ) )
            return false;

        DataPacket cp = (DataPacket)obj;

        boolean acc= true;

        int min = ( cp.getData().length < this.information.length) ?
                cp.getData().length:
                this.information.length;

        for(int i=0; i< min; i++)
            acc = acc && (cp.getData()[i] == this.information[i]);

        return acc &&
                (cp.getFlag().equals(this.flag)) &&
                (cp.getSeq() == this.seq) &&
                (cp.getWindow() == this.window) &&
                (cp.getTimestamp() == this.timestamp);

    }
}
