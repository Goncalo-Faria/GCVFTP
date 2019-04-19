package Transport.Receiver;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class Interval<V> {

    int min;
    int max;

    Interval(int x){
        this.min = x;
        this.max = x;
    }

    public abstract V take();

    public synchronized boolean greater(Interval x){
        /*argument is greater than*/

        return (this.max < x.min );
    }

    public synchronized boolean isSingleton(){
        return this.max == this.min;
    }

    public synchronized boolean greater(int x){
        /*argument is greater than*/
        return (this.max < x );
    }

    public synchronized  boolean less(Interval x){
        Interval a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        Interval b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;

        synchronized (a) {
            synchronized (b) {
                /*argument is less than*/
                return (this.min > x.max);
            }
        }
    }

    public synchronized boolean less(int x){
        /*argument is less than*/
        return (this.min > x );
    }

    public boolean intersects(Interval x){
        /*basta que um dos limites de x esteja no intervalo*/
        Interval a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        Interval b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;

        synchronized (a) {
            synchronized (b) {
                return (x.max <= this.max && x.max >= this.min) || (x.min <= this.max && x.min >= this.min);
            }
        }
    }

    public boolean contains(Interval x){
        Interval a = ( this.toString().compareTo(x.toString()) < 0 ) ? this : x;
        Interval b = ( this.toString().compareTo(x.toString()) < 0 ) ? x : this;

        synchronized (a) {
            synchronized (b) {
                return (this.min <= x.min) && (x.min <= this.max) && (this.min <= x.max) && (x.max <= this.max);
            }
        }
    }

    public synchronized boolean contains(int x){
        return ( this.min <= x ) && ( x <= this.max );
    }

    public abstract boolean merge( Interval i );

}
