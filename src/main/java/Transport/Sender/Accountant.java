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

/* structure for 'accounting' traveling packets*/
class Accountant {

    private LinkedBlockingQueue<DataPacket> uncounted;
    /*
     * List containing the packets in order that haven't been acked or are waiting to be sent.
     *
     * TODO : Make it a circular List.
     * */
    private ConcurrentSkipListMap<Integer,StreamState> streams = new ConcurrentSkipListMap<Integer,StreamState>(); // stream_id -> stream_state
    /*
     * Map that contains information of a stream state.
     * */
    private LinkedBlockingQueue<DataPacket> sending = new LinkedBlockingQueue<DataPacket>();
    /*
     * Packet Sending queue.
     *      the elements of the queue are either :
     *      - datapackets.
     *      - null when refering to the element in the top.
     *
     * obs: why null? we want to keep the order in control packets.
     * */
    /*
     * List of control packets waiting to be sent.
     * */
    private AtomicBoolean at = new AtomicBoolean(true);
    /*
     * true : if the accountant is active.
     * false : otherwise.
     * */
    private FlowWindow window;
    /*
     * Adjusts the number of packets that can be sent per SYN based on connection state information.
     * */
    private AtomicInteger seq;/* TODO : make sure 0 > the max numbers */
    /*
     * Current sequence number.
     *  */

    private ReentrantLock rl = new ReentrantLock();
    /*
     * Usado para permitir a cada stream esperar pelo envio dos seus pacotes.
     * */

    public Accountant(int stock, int seq, FlowWindow window){
        this.uncounted = new LinkedBlockingQueue<DataPacket>(stock);
        this.seq = new AtomicInteger(seq);
        this.window = window;

    }

    void data( DataPacket value) throws InterruptedException{

        /* espera pela oportunidade para colocar o pacote no sistema*/

        int stream_id = value.getMessageNumber();
        /* extrai o número de stream do pacote*/

        if( this.streams.containsKey(stream_id) ){
            /* regista a mensagem como pertencendo ao stream */
            this.streams.get(stream_id).increment(value);
        }else{
            /* cria um novo stream */
            this.streams.put(stream_id,new StreamState(value,this.rl.newCondition()));
        }

        value.setSeq(this.seq());
        uncounted.put(value);

        /* atribui um número de sequência ao datapacket */

        sending.put(value);
        /* põe o datapacket na fila de envio */
    }

    void ack(int x) throws NotActiveException, InterruptedException{
        if( !this.at.get() )
            throw new NotActiveException();

        DataPacket packet;

        do{
            packet = this.uncounted.take();
            this.streams.get(packet.getMessageNumber()).decrement();
            /* decrementa o número de pacotes em falta do stream*/

        }while( packet.getSeq() < x );/* todos os pacotes com número de sequência inferior */
        /* TODO: Assegurar que é suportada ordem circular */

    }

    void nack(List<Integer> missing) throws InterruptedException, NotActiveException {

        if( !this.at.get() )
            throw new NotActiveException();

        Iterator<DataPacket> it = this.uncounted.iterator();

        for( Integer mss : missing)
            while (it.hasNext()) {
                DataPacket packet = it.next();
                if(packet.getSeq() == mss) { this.sending.put(packet); }
            }
    }

    public boolean hasTerminated(){
        return !this.at.get();/*queries if the accountant has termianted*/
    }

    void terminate(){
        /*deactivates the accountat*/
        at.set(false);

        this.uncounted.clear();
        this.streams.clear();
        this.sending.clear();

    }

    private int seq(){
        /*TODO: assegurar que é um lista circular */
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

            while ( st.hasfinished() )/* espera passivamente pelo o termino do stream*/
                st.await();
        }finally {
            this.rl.unlock();
        }

        this.streams.remove(stream_id);/* remove o stream do accountant*/

    }

    DataPacket get() throws InterruptedException, NotActiveException{
        /* método para o sendworker */
        if( !this.at.get() )
            throw new NotActiveException();

        DataPacket index = sending.take();

        // procura o index
        return index;
    }

    DataPacket poll() throws InterruptedException, NotActiveException{
        /* método para o sendworker */
        if( !this.at.get() )
            throw new NotActiveException();

        DataPacket index = sending.poll();

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
