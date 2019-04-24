package Transport.Receiver;

import Transport.Executor;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.io.NotActiveException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

class Examiner {

    private LinkedBlockingQueue<ControlPacket> control;
    private LinkedBlockingQueue<DataPacket> data = new LinkedBlockingQueue<DataPacket>();
    private SimpleSeqChain uncounted;
    private AtomicInteger lastOkedSeq;
    private AtomicBoolean active = new AtomicBoolean(true);
    private final int maxDataBufferSize;

    Examiner(int maxControlBufferSize, int maxDataBufferSize, int seq){
        System.out.println(">>>>> theirs " + seq + "<<<<<<<");
        this.control = new LinkedBlockingQueue<>(maxControlBufferSize);
        this.uncounted =  new SimpleSeqChain(maxDataBufferSize);
        this.lastOkedSeq = new AtomicInteger(seq);
        this.maxDataBufferSize = maxDataBufferSize;
    }

    int getWindowSize(){
        return this.maxDataBufferSize - this.control.size();
    }

    void supply(Packet packet) throws InterruptedException, NotActiveException{
        if( !this.active.get() )
            throw new NotActiveException();

        if( packet instanceof ControlPacket)
            this.control((ControlPacket)packet);

        else if( packet instanceof DataPacket)
            this.data((DataPacket)packet);

    }

    int getLastOk(){
        return lastOkedSeq.get();
    }

    private void data(DataPacket packet) throws NotActiveException{
        if( !this.active.get() )
            throw new NotActiveException();

        if( packet.getSeq() > this.lastOkedSeq.get() ){
            uncounted.add(packet);
            /* verificar se posso tirar acks*/

            if (lastOkedSeq.get() + 1 == uncounted.minSeq()) {
                IntervalPacket p = uncounted.take();

                lastOkedSeq.set(p.max());

                List<DataPacket> lisp = p.getpackets();

                this.data.addAll(lisp);

                lisp.forEach(
                        lisppacket ->
                        {
                            try {
                                Executor.add(Executor.ActionType.DATA);
                            } catch (Exception e) {
                                e.getStackTrace();
                            }
                        }
                );
            }
        }
    }

    private void control(ControlPacket packet) throws InterruptedException, NotActiveException{
        if( !this.active.get() )
            throw new NotActiveException();

        Executor.add(Executor.ActionType.CONTROL);

        control.put(packet);
    }

    ControlPacket getControlPacket() throws InterruptedException{
        return this.control.take();
    }

    DataPacket getDataPacket() throws InterruptedException{
        return this.data.take();
    }

    List<Integer> getLossList(){
        return uncounted.dual();
    }

    void terminate(){
        this.active.set(false);

        control.clear();
        uncounted.clear();
    }
}
