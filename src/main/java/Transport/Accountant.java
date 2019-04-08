package Transport;

import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import javax.xml.crypto.Data;
import java.io.NotActiveException;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/* structure for saving traveling packets*/
public class Accountant {

    private ConcurrentSkipListMap<Integer, DataPacket> uncounted = new ConcurrentSkipListMap<>(); // ack -> packet
    private ConcurrentSkipListMap<Integer,StreamState> streams = new ConcurrentSkipListMap<>(); // stream_id -> stream_state
    private LinkedBlockingDeque<Integer> sending = new LinkedBlockingDeque<>();
    private LinkedBlockingQueue<ControlPacket> control = new LinkedBlockingQueue<>();
    private AtomicBoolean at = new AtomicBoolean(true);
    private long stock;

    private AtomicInteger flow_window = new AtomicInteger(2); /* sempre que for alterada é lançado um signal All*/
    private AtomicInteger storage = new AtomicInteger(0);

    private ReentrantLock rl = new ReentrantLock();
    private Condition fifo = rl.newCondition();

    public Accountant(long stock){
        this.stock = stock;
    }

    void data(Integer id, DataPacket value) throws InterruptedException{

        int stream_id = value.getMessageNumber();

        if( this.streams.containsKey(stream_id) ){
            this.streams.get(stream_id).increment(value);
        }else{
            this.streams.put(stream_id,new StreamState(value,this.rl.newCondition()));
        }

        try {
            this.rl.lock();
            while (this.storage.get() > this.stock * this.flow_window.get())
                this.fifo.await();
        }finally {
            this.rl.unlock();
        }

        uncounted.put(id, value);
        sending.putLast(id);

        this.storage.addAndGet(1);
    }

    void ack(int x) throws NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();

        ConcurrentNavigableMap<Integer,DataPacket> tmp = uncounted.headMap(x,true);

        for(Map.Entry<Integer,DataPacket> ent : tmp.entrySet()){
            int stream_id = ent.getValue().getMessageNumber();
            this.streams.get(stream_id).decrement();
        }

        this.storage.addAndGet(-tmp.size());

        tmp.clear();
    }

    int window() throws NotActiveException {
        if( !this.at.get() )
            throw new NotActiveException();

        return this.flow_window.get();
    }
    void control(ControlPacket p) throws InterruptedException, NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();

        sending.putFirst(-1);
        control.put(p);
    }

    void nack(List<Integer> missing) throws InterruptedException, NotActiveException {
        if( !this.at.get() )
            throw new NotActiveException();

        for (Integer e : missing)
            sending.putFirst(e);
    }

    public boolean hasTerminated(){
        return !this.at.get();
    }

    void terminate(){
        at.set(false);

        this.uncounted.clear();

        /* saving variable values */
        long tmpstock = this.stock;
        int tmpwindow = this.flow_window.get();

        /* leting the blocked threads leave*/
        this.stock = 1;
        this.flow_window.set(Integer.MAX_VALUE);
        this.rl.lock();
        try {
            this.fifo.signalAll();
        }finally {
            this.rl.unlock();
        }

        /* restauring variable values*/
        this.stock = tmpstock;
        this.flow_window.set(tmpwindow);
    }

    public void finish(int stream_id) throws InterruptedException, NotActiveException, StreamCorruptedException {
        if( !this.at.get() )
            throw new NotActiveException();

        this.rl.lock();
        try{
            StreamState st = this.streams.get(stream_id);

            while ( st.hasfinished() )
                st.await();
        }finally {
            this.rl.unlock();
        }

    }

    Packet get() throws InterruptedException, NotActiveException{

        if( !this.at.get() )
            throw new NotActiveException();

        int index = sending.take();

        if( index == -1)
            return control.take();

        this.rl.lock();
        try {
            this.fifo.signal();
        }finally {
            this.rl.unlock();
        }

        return uncounted.get( index);
    }

    class StreamState {

        private boolean itsfinal;
        private Condition finished;
        private int count = 1;

        StreamState(DataPacket p, Condition c){
            this.itsfinal = p.getFlag().equals(DataPacket.Flag.SOLO) ;
            this.finished = c;
        }

        synchronized void increment(DataPacket p){
            this.itsfinal = this.itsfinal || (p.getFlag().equals(DataPacket.Flag.LAST));
            count++;
        }

        synchronized void decrement(){
            count--;

            if(count == 0)
                finished.signalAll();

        }

        synchronized boolean hasfinished(){
            return itsfinal && (count == 0);
        }

        void await() throws InterruptedException{
            this.finished.await();
        }

    }

}
