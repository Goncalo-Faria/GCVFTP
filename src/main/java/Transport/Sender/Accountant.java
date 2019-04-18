package Transport.Sender;

import Transport.FlowWindow;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.io.NotActiveException;
import java.io.StreamCorruptedException;
import java.util.*;
import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/* structure for saving traveling packets*/
class Accountant {

    private LinkedBlockingDeque<DataPacket> uncounted = new LinkedBlockingDeque<DataPacket>();
    private ConcurrentSkipListMap<Integer,StreamState> streams = new ConcurrentSkipListMap<Integer,StreamState>(); // stream_id -> stream_state
    private LinkedBlockingDeque<Packet> sending = new LinkedBlockingDeque<Packet>();
    private LinkedBlockingQueue<ControlPacket> control = new LinkedBlockingQueue<ControlPacket>();
    private AtomicBoolean at = new AtomicBoolean(true);
    private long buffersize;

    private FlowWindow window; /* sempre que for alterada é lançado um signal All*/
    private AtomicInteger storage = new AtomicInteger(0);
    private AtomicInteger seq;

    private ReentrantLock rl = new ReentrantLock();
    private Condition fifo = rl.newCondition();

    public Accountant(long stock, int seq,FlowWindow window){
        this.buffersize = stock;
        this.seq = new AtomicInteger(seq);
        this.window = window;
        this.window.warn(this.fifo);
    }

    void data( DataPacket value) throws InterruptedException{

        int stream_id = value.getMessageNumber();

        if( this.streams.containsKey(stream_id) ){
            this.streams.get(stream_id).increment(value);
        }else{
            this.streams.put(stream_id,new StreamState(value,this.rl.newCondition()));
        }

        this.rl.lock();
        try {
            while (this.storage.get() > this.buffersize)
                this.fifo.await();

            value.setSeq(this.seq());
            uncounted.putLast(value);
        }finally {
            this.rl.unlock();
        }

        sending.putLast(value);

        this.storage.addAndGet(1);
    }

    void ack(int x) throws NotActiveException, InterruptedException{
        if( !this.at.get() )
            throw new NotActiveException();

        DataPacket packet;

        do{
            packet = this.uncounted.takeFirst();
            this.storage.addAndGet(-1);
            this.streams.get(packet.getMessageNumber()).decrement();

        }while( packet.getSeq() < x );

    }

    void control(ControlPacket p) throws InterruptedException, NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();

        sending.putFirst(null);
        control.put(p);
    }

    void nack(List<Integer> missing) throws InterruptedException, NotActiveException {
        /* Assuming it's sorted*/

        /*
        mete na lista de entrega os pacotes no nack
        como os pacotes serão muito provavelmente os mais antigos
        começa a procurar da cabeça até à cauda
        */

        if( !this.at.get() )
            throw new NotActiveException();

        Iterator<DataPacket> it = this.uncounted.iterator();

        for( Integer mss : missing)
            while (it.hasnext()) {
                DataPacket p = it.next();
                if(p.getSeq() == mss) { this.sending.putFirst(p); }
            }
    }

    public boolean hasTerminated(){
        return !this.at.get();
    }

    void terminate(){
        at.set(false);

        this.uncounted.clear();

        /* saving variable values */
        long tmpstock = this.buffersize;

        /* leting the blocked threads leave*/
        this.buffersize = Integer.MAX_VALUE;

        this.rl.lock();
        try {
            this.fifo.signalAll();
        }finally {
            this.rl.unlock();
        }

        /* restauring variable values*/
        this.buffersize = tmpstock;

        this.window.purge(this.fifo);
    }

    private int seq(){
        return this.seq.accumulateAndGet(0,
                (x,y) -> Integer.max(++x % Integer.MAX_VALUE, y)
        );
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

        this.streams.remove(stream_id);

    }

    Packet get() throws InterruptedException, NotActiveException{

        if( !this.at.get() )
            throw new NotActiveException();

        Packet index = sending.takeFirst();

        if (index == null)
                return control.take();

        this.rl.lock();
        try {
            this.fifo.signal();
        } finally {
            this.rl.unlock();
        }
            // procura o index
        return index;
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
