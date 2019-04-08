package Transport;

import Transport.Unit.Packet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* structure for saving traveling packets*/
public class Accountant<E> {

    private ConcurrentSkipListMap<Integer,E> uncounted = new ConcurrentSkipListMap<>();
    private LinkedBlockingDeque<Integer> sending = new LinkedBlockingDeque<>();
    private LinkedBlockingQueue<E> control = new LinkedBlockingQueue<E>();
    private AtomicBoolean at = new AtomicBoolean(false);
    private CountDownLatch replenishment_rate_alarm = new CountDownLatch(1);
    private long stock = 20;
    private long level = 40;
    private AtomicInteger flow_window = new AtomicInteger(2);

    public Accountant(long stock, long level){
        this.stock = stock;
        this.level = level;
    }

    void data(Integer id, E value) throws InterruptedException{
        uncounted.put(id, value);
        sending.putLast(id);

        if( this.sending.size() > (this.stock + this.level) * this.flow_window.get() )
            replenishment_rate_alarm = new CountDownLatch(1);
    }

    void ack(int x){
        uncounted.headMap(x,true).clear();
    }

    int window(){
        return this.flow_window.get();
    }
    void control(E p) throws InterruptedException{
        sending.putFirst(-1);
        control.put(p);
    }

    void nack(List<Integer> missing) throws InterruptedException{

        for (Integer e : missing)
            sending.putFirst(e);
    }

    public boolean isOver(){
        return (this.uncounted.size() == 0);
    }

    boolean hasnext(){
        return (sending.size() == 0);
    }

    void terminate(){
        at.set(true);
    }

    public void await() throws InterruptedException{
        replenishment_rate_alarm.await();
    }

    E get() throws InterruptedException{

        int index = sending.take();

        if( index == -1)
            return control.take();

        if( this.sending.size() < this.stock * this.flow_window.get() ) {
            replenishment_rate_alarm.countDown();
        }

        return uncounted.get( index);
    }
}
