package Transport.Receiver;

import Transport.Unit.DataPacket;

import java.util.LinkedList;
import java.util.ListIterator;

public class SimpleSeqChain<V> {

    LinkedList<IntervalPacket> list = new LinkedList<>();
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    private int amplitude;

    public SimpleSeqChain( int maxamplitude ){
        this.amplitude = maxamplitude;
    }

    public synchronized void add(DataPacket packet){

        int seq = packet.getSeq();

        if( seq > amplitude + min)
            return;

        this.min = ( this.min > seq ) ? seq : this.min;
        this.max = ( this.max < seq ) ? seq : this.max;

        IntervalPacket ip = new IntervalPacket(packet.getSeq(),packet);
        ListIterator<IntervalPacket> it = this.list.listIterator();

        while( it.hasNext() ){
            IntervalPacket cur = it.next();

            if( cur.merge(ip) )
                return;

            if( ip.less(cur) ) {
                it.add(ip);
                return;
            }

        }

        list.addLast(ip);
    }

    public synchronized int minseq(){ return this.min; }

    public synchronized int maxseq(){ return this.max; }

    public synchronized IntervalPacket take(){

        if( list.size() == 0 )
            return null;

        if( list.size() == 1 ){
            min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
            return list.removeFirst();
        }

        IntervalPacket t = list.removeFirst();

        this.min = list.peek().min();

        return t;
    }
}
