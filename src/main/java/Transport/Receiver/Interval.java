package Transport.Receiver;

public abstract class Interval<V> {

    int min;
    int max;

    Interval(int x){
        this.min = x;
        this.max = x;
    }

    public abstract V get( int x);

    public boolean greater(Interval x){
        /*argument is greater than*/
        return (this.max < x.min );
    }

    public boolean isSingleton(){
        return this.max == this.min;
    }

    public boolean greater(int x){
        /*argument is greater than*/
        return (this.max < x );
    }

    public boolean less(Interval x){
        /*argument is less than*/
        return (this.min > x.max );
    }

    public boolean less(int x){
        /*argument is less than*/
        return (this.min > x );
    }

    public boolean intersects(Interval x){
        /*basta que um dos limites de x esteja no intervalo*/
        return (x.max <= this.max && x.max >= this.min ) || (x.min <= this.max && x.min >= this.min );
    }

    public boolean contains(Interval x){
        return ( this.min <= x.min ) && ( x.min <= this.max) && ( this.min <= x.max ) && ( x.max <= this.max);
    }

    public boolean contains(int x){
        return ( this.min <= x ) && ( x <= this.max);
    }

    public abstract boolean merge( Interval i );

}
