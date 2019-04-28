package Transport.Sender;

import Transport.FlowWindow;
import Transport.Unit.DataPacket;

import java.io.NotActiveException;
import java.util.*;
import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/* structure for 'accounting' traveling packets*/
class Accountant {

    private LinkedBlockingQueue<DataPacket> uncounted;
    /*
     * List containing the packets in order that haven't been acked or are waiting to be sent.
     *
     * TODO : Make it a circular List.
     * */
    private LinkedBlockingDeque<DataPacket> sending = new LinkedBlockingDeque<DataPacket>();
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
    private AtomicBoolean ative = new AtomicBoolean(true);
    /*
     * true : if the accountant is active.
     * false : otherwise.
     * */

    private AtomicInteger seq;/* TODO : make sentSure 0 > the max numbers */
    /*
     * Current sequence number.
     *  */

    Accountant(int stock, int seq){
        this.uncounted = new LinkedBlockingQueue<DataPacket>(stock);
        this.seq = new AtomicInteger(seq);
    }

    void data( DataPacket value) throws InterruptedException{

        /* espera pela oportunidade para colocar o pacote no sistema*/

        value.setSeq(this.seq());
        uncounted.put(value);

        /* atribui um número de sequência ao datapacket */

        sending.put(value);
        /* põe o datapacket na fila de envio */
    }

    void ok(int x) throws NotActiveException, InterruptedException{
        if( !this.ative.get() )
            throw new NotActiveException();

        DataPacket packet;
        if( !this.uncounted.isEmpty() ) {
            do {
                packet = this.uncounted.take();

                /* decrementa o número de pacotes em falta do stream*/

            } while (packet.getSeq() < x);/* todos os pacotes com número de sequência inferior */
            /* TODO: Assegurar que é suportada ordem circular */
        }

    }

    void nope(List<Integer> missing) throws InterruptedException, NotActiveException {

        if( !this.ative.get() )
            throw new NotActiveException();

        Iterator<DataPacket> it = this.uncounted.iterator();
        LinkedList<DataPacket> temporaryCache = new LinkedList<>();
        try{
            System.out.println("loss: " + missing.get(0) + " seq: " + uncounted.peek().getSeq());
        }catch (NullPointerException e){
            System.out.println("nullps");
        }
        for( Integer mss : missing ) {
            while (it.hasNext()) {
                DataPacket packet = it.next();
                if (packet.getSeq() == mss) {
                    System.out.println(" :: " + mss);
                    temporaryCache.addFirst(packet);
                    break;
                }
            }
        }

        for( DataPacket e : temporaryCache)
            this.sending.putFirst(e);

        temporaryCache.clear();
    }

    void terminate(){
        /*deactivates the accountant*/
        ative.set(false);
        this.uncounted.clear();
        this.sending.clear();
    }

    private int seq(){
        /*TODO: assegurar que é um lista circular */
        return this.seq.accumulateAndGet(0,
                (x,y) -> Integer.max(++x % Integer.MAX_VALUE, y)
        );
    }

    DataPacket poll() throws NotActiveException{
        /* método para o sendworker */
        if( !this.ative.get() )
            throw new NotActiveException();

        DataPacket index = sending.poll();

        // procura o index
        return index;
    }

}
