package Transport.Receiver;

import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Examiner implements Runnable{

    private LinkedBlockingQueue<ControlPacket> control;
    private SimpleSeqChain uncounted;
    private AtomicInteger last_acked_seq;

    public Examiner(int maxcontrol, int maxdata,int seq){
        this.control = new LinkedBlockingQueue<>(maxcontrol);
        this.uncounted =  new SimpleSeqChain(maxdata);
        this.last_acked_seq = new AtomicInteger(seq);
    }

    public void supply(Packet packet) throws InterruptedException{

        if( packet instanceof ControlPacket)
            this.control((ControlPacket)packet);

        else if( packet instanceof DataPacket)
            this.data((DataPacket)packet);

    }

    public int getLastAck(){
        return last_acked_seq.get();
    }

    public void incAck(){
        last_acked_seq.incrementAndGet();
    }

    void data(DataPacket packet) throws InterruptedException{
        uncounted.add(packet);
    }

    void control(ControlPacket packet) throws InterruptedException{
        control.put(packet);
    }

    public void run(){

    }
}
