package Transport;

import Transport.Unit.Packet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.BlockingDeque;

import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/* structure for saving traveling packets*/
public class Accountant<E> {

    private ConcurrentSkipListMap<Integer,E> uncounted = new ConcurrentSkipListMap<>();
    private LinkedBlockingDeque<Integer> sending = new LinkedBlockingDeque<>();
    private LinkedBlockingQueue<E> control = new LinkedBlockingQueue<E>();
    private AtomicBoolean at = new AtomicBoolean(false);

    void data(Integer id, E value) throws InterruptedException{
        uncounted.put(id, value);
        sending.putLast(id);
    }
    void ack(int x){
        uncounted.headMap(x,true).clear();
    }

    void control(E p) throws InterruptedException{
        sending.putFirst(-1);
        control.put(p);
    }

    void nack(List<Integer> missing) throws InterruptedException{

        for (Integer e : missing)
            sending.putFirst(e);
    }

    boolean hasnext(){
        return (sending.size() == 0);
    }

    void terminate(){
        at.set(true);
    }

    E get() throws InterruptedException{

        int index = sending.take();

        if( index == -1)
            return control.take();
        return uncounted.get( index);
    }
}
