package Transport.Receiver;

import Transport.Unit.Packet;

import java.util.HashMap;

class IntervalPacket extends Interval<Packet> {

    private HashMap<Integer,Packet> l = new HashMap<Integer,Packet>();

    public IntervalPacket(int x, Packet p){
        super(x);
        l.put(x,p);
    }

    public Packet get(int x){

        return l.get(x);

    }

    public boolean merge( Interval i ){

        int init_min = this.min;
        int init_max = this.max;

        if( (i.min == this.max + 1) || (i.max + 1 == this.min) )
        {/*se são contiguos integra*/
            this.min = (this.min < i.min) ? this.min : i.min;
            this.max = (this.max > i.max) ? this.max : i.max;
        }

        return (this.min == init_min) && (this.max == init_max);
        /*indica se o merge foi feito*/
    }

    public HashMap<Integer,Packet> getMap(){
        return l;
    }

    public boolean merge( IntervalPacket i ){

        int sz = l.size();
        if( (i.min == this.max + 1) || (i.max + 1 == this.min) )
        {/*se são contiguos integra*/

            l.putAll(i.getMap());

            this.min = (this.min < i.min) ? this.min : i.min;
            this.max = (this.max > i.max) ? this.max : i.max;
        }

        return (sz != l.size());
    }

}
