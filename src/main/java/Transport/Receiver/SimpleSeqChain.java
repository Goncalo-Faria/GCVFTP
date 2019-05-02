package Transport.Receiver;

import Test.Debugger;
import Transport.Common.LList;
import Transport.Unit.DataPacket;

import java.util.LinkedList;
import java.util.List;

public class SimpleSeqChain {

    LList<IntervalPacket> list = new LList<>();
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    private final int maxAmplitude;
    //private ReadWriteLock wrl = new ReentrantReadWriteLock();

    public SimpleSeqChain( int maxAmplitude ){
        this.maxAmplitude = maxAmplitude;
    }

    public int size(){
        int s = 0;

            LList<IntervalPacket> v = list.view();
            v.start();

            while( v.hasNext() ) {
                IntervalPacket cur = v.next().value();
                s += cur.max() - cur.min();
            }

        return s;
    }

    public void add(DataPacket packet){

            int seq = packet.getSeq();

            if( seq > maxAmplitude + min && !list.empty() ){
                Debugger.log(" DROP ::: < " + seq);

                return;
            }


            this.min = ( this.min > seq ) ? seq : this.min;
            this.max = ( this.max < seq ) ? seq : this.max;

            IntervalPacket ip = new IntervalPacket(packet);

            list.start();

            while( list.hasNext() ){
                IntervalPacket cur = list.next().value();

                int merged = cur.merge(ip);

                if( merged == 1 ){
                    /*check for recursive agregation */
                    ip = cur;
                    list.remove();

                }else if( merged == -1 ) {
                    return;

                }else if( merged == 0 ){
                    return;
                }else if( ip.less(cur) ){
                    list.add(ip); /* add before the iterator mark */
                    return      ;
                }
            }
            list.next().add(ip);/* coloca na cauda.*/

    }

    public int minSeq(){

            return this.min;

    }

    public int maxSeq(){

            return this.max;

    }

    public List<Integer> dual( int startElem, int maxSize){

            List<Integer> dualRep = new LinkedList<>();

            LList<IntervalPacket> v = list.view();

            v.start();

            IntervalPacket pst = null,cur;
            int i = 0;
            while(v.hasNext() && (i < maxSize) ){
                i++;
                cur = v.next().value();
                if( cur != null && pst != null ){
                    dualRep.add(pst.max() + 1);
                    dualRep.add(cur.min() - 1);
                }
                pst = cur;
            }

            if( !dualRep.isEmpty() ){
                dualRep.add(0,this.min-1);//to
                dualRep.add(0, startElem);//from

                dualRep.add(this.max + 1 );
                dualRep.add(this.max + 1 );

            }

            return dualRep;

    }

    public IntervalPacket take(){

            if( list.empty() )
                return null;

            if( list.singleton() ){
                min = Integer.MAX_VALUE;
                max = Integer.MIN_VALUE;
                IntervalPacket e = list.start().next().value();
                list.remove();
                return e;
            }

            IntervalPacket t = list.start().next().value();
            list.remove();

            this.min = list.peek().min();

            return t;
    }

    public IntervalPacket peek(){

        return list.peek();

    }

    public void clear(){

            this.min = Integer.MAX_VALUE;
            this.max = Integer.MIN_VALUE;
            list = new LList<>();
    }
}
