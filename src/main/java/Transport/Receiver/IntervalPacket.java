package Transport.Receiver;

import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class IntervalPacket {

    private int min;
    private int max;

    private LinkedList<DataPacket> l = new LinkedList<>();

    public IntervalPacket( DataPacket p){
        this.min = this.max = p.getSeq();
        l.add(p);
    }

    public synchronized int min(){
        return min;
    }

    public synchronized int max(){
        return max;
    }

    public synchronized boolean isSingleton(){
        return this.max == this.min;
    }

    public synchronized  boolean less(IntervalPacket x){
        IntervalPacket a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        IntervalPacket b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;

        synchronized (a) {
            synchronized (b) {
                /*argument is less than*/
                return (this.max < x.min());
            }
        }
    }

    public synchronized boolean less(int x){
        /*argument is less than*/
        return (this.min > x );
    }

    public boolean intersects(IntervalPacket x){
        /*basta que um dos limites de x esteja no intervalo*/
        IntervalPacket a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        IntervalPacket b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;

        synchronized (a) {
            synchronized (b) {
                return (x.max() <= this.max && x.max() >= this.min()) || (x.min() <= this.max && x.min() >= this.min);
            }
        }
    }

    public boolean canMerge(IntervalPacket x){
        IntervalPacket a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        IntervalPacket b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;

        synchronized (a) {
            synchronized (b) {

                return (x.min() == this.max + 1) || (x.max() + 1 == this.min);
            }
        }
    }

    public synchronized boolean contains(int x){
        return ( this.min <= x ) && ( x <= this.max );
    }

    public IntervalPacket(int x, DataPacket p){
        l.add(p);
    }

    public synchronized Packet take(){
        this.min++;
        return l.poll();

    }

    public LinkedList<DataPacket> getpackets(){
        return new LinkedList<DataPacket>(this.l);
    }

    public int merge( IntervalPacket x ){

        IntervalPacket a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        IntervalPacket b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;
        //System.out.println(x.min() + " - " + " - " + x.max());
        //System.out.println(this.min + " - " + " - " + this.max);


        synchronized (a) {
            synchronized (b) {

                if ( x.min() == this.max + 1 ) {/*se são contiguos integra*/
                    this.max = x.max();

                    this.l.addAll( x.getpackets() );
                    System.out.println(" .. ");
                    return 1;
                }else if( x.max() + 1 == this.min ){
                    this.min = x.min();
                
                    this.l.addAll( 0, x.getpackets() );
                    System.out.println(" .. ");
                    return -1;
                }

                return 0;
                /*indica se o merge foi feito*/
            }
        }
    }

}
