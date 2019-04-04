package Transport.Unit;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

public class DataPacket extends Packet {

    public enum Flag{
        MIDDLE,
        LAST,
        FIRST,
        SOLO;

        void mark( BitSet bs , Flag s){
            switch( s ){
                case MIDDLE:
                    bs.set(0,false); bs.set(1,false);
                    break;
                case LAST:
                    bs.set(0,false); bs.set(1,true);
                    break;
                case FIRST:
                    bs.set(0,true); bs.set(1,false);

                    break;
                case SOLO:
                    bs.set(0,true); bs.set(1,true);
                    break;
            }
        }

        Flag parse(BitSet s){

            int sum=0;

            if ( s.get(0) ){
                sum+=2;
            }

            if( s.get(1) ){
                sum++;
            }
            return Flag.values()[sum];
        }
    }

    private int timestamp=0;
    private int seq=0;
    private int window=0;
    private byte[] information = new byte[0];
    private Flag flag;


    DataPacket( BitSet data, int size ){
        this.timestamp = this.extract_timestamp(data);
        this.seq = this.extract_seq(data);
        this.window = this.extract_window(data);
        this.flag = this.extract_flag(data);
        this.information = data.get(96, size*8).toByteArray();
    }

    public DataPacket(byte[] data, int timestamp, int seq, int window, DataPacket.Flag flag){
        this.information = data;
        this.timestamp = timestamp;
        this.seq = seq;
        this.window = window;
        this.flag = flag;
    }

    private int extract_timestamp( BitSet data ){
        int ans = ByteBuffer.wrap(data.get(32,64).toByteArray()).getInt();

        //System.out.println( "timestamp " + ans);
        return ans;
    }

    private int extract_seq( BitSet data ){
        BitSet b = data.get(0,32);

        b.set(0,false);

        int ans = ByteBuffer.wrap(b.toByteArray()).getInt();

        //System.out.println( "seq "+ ans);

        return ans;
    }

    private int extract_window( BitSet data){

        BitSet s = BitSet.valueOf(ByteBuffer.wrap(data.get(64,96).toByteArray()));
        s.set(0,false);
        s.set(1,false);

        int ans = ByteBuffer.wrap(s.toByteArray()).getInt();

        //System.out.println("window : " + ans);

        return ans;
    }

    private Flag extract_flag( BitSet data){
        Flag f =  Flag.SOLO.parse(data.get(64,66));

        //System.out.println("flag : " + f);
        return f;
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

        ByteBuffer b = ByteBuffer.allocate(3 * 4 + this.information.length);

        byte[] answer = b.array();

        ByteBuffer basebs = ByteBuffer.allocate(4);
        ByteBuffer viewbs = ByteBuffer.wrap(basebs.array());

        basebs.putInt(this.seq);

        BitSet seqbs = BitSet.valueOf(viewbs);

        seqbs.set(0,false);

        b.put(seqbs.toByteArray()).putInt(this.timestamp);

        ByteBuffer baseb = ByteBuffer.allocate(4);
        ByteBuffer viewb = ByteBuffer.wrap(baseb.array());

        baseb.putInt(this.window);

        BitSet bs = BitSet.valueOf(viewb);

        this.flag.mark(bs,this.flag);

        b.put(bs.toByteArray()).put(this.information);

        return answer;
    }

    @Override
    public boolean equals(Object obj) {
        if( !( obj instanceof DataPacket ) )
            return false;

        DataPacket cp = (DataPacket)obj;

        boolean acc= true;

        int min = ( cp.getData().length < this.information.length) ? cp.getData().length: this.information.length;

        for(int i=0; i< min; i++)
            acc = acc && (cp.getData()[i] == this.information[i]);

        return acc && cp.getFlag().equals(this.flag) &&
                (cp.getSeq() == this.seq) &&
                (cp.getWindow() == this.window) && (cp.getTimestamp() == this.timestamp);

    }
}
