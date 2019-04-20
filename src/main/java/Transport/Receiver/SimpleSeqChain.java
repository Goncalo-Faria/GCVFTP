package Transport.Receiver;

import Transport.Unit.DataPacket;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SimpleSeqChain {

    LList<IntervalPacket> list = new LList<>();
    private int min = Integer.MAX_VALUE;
    private int max = Integer.MIN_VALUE;
    private int amplitude;
    private ReadWriteLock wrl = new ReentrantReadWriteLock();

    public SimpleSeqChain( int maxamplitude ){
        this.amplitude = maxamplitude;
    }

    public void add(DataPacket packet){
        wrl.writeLock().lock();
        try{
            int seq = packet.getSeq();

            if( seq > amplitude + min && !list.empty() )
                return;

            this.min = ( this.min > seq ) ? seq : this.min;
            this.max = ( this.max < seq ) ? seq : this.max;

            IntervalPacket ip = new IntervalPacket(packet);
            boolean append = true;

            list.start();

            while( list.hasNext() ){
                IntervalPacket cur = list.next().value();

                int merged = cur.merge(ip);

                if( merged > 0 ){
                    /*check for recursive agregation */
                    ip = cur;
                    list.remove();

                }else if( merged < 0){
                    return ;
                }else if( ip.less(cur) ){
                    list.add(ip); /* add before the iterator mark */
                    return      ;
                }
            }
            list.next().add(ip);/* coloca na cauda.*/

            list.start();
        }finally{
            wrl.writeLock().unlock();
        }
    }

    public int minseq(){ 
        wrl.readLock().lock();
        try{ 
            return this.min;
        }finally{ 
            wrl.readLock().unlock(); 
        }
    }

    public int maxseq(){ 
        wrl.readLock().lock();
        try{ 
            return this.max;
        }finally{ 
            wrl.readLock().unlock(); 
        }
    }


    public IntervalPacket take(){
        wrl.writeLock().lock();
        try{

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
        }finally{
            wrl.writeLock().unlock();
        }
    }

    public IntervalPacket peek(){
        wrl.readLock().lock();
        try {
            return list.peek();
        }finally {
            wrl.readLock().unlock();
        }
    }

    public void clear(){
        wrl.writeLock().lock();
        try{
            this.min = Integer.MAX_VALUE;
            this.max = Integer.MIN_VALUE;
            list = new LList<>();
        }finally{
            wrl.writeLock().unlock();
        }
    }
}
