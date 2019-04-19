package Transport.Receiver;

import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.io.NotActiveException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Examiner {

    private LinkedBlockingQueue<ControlPacket> control;
    private SimpleSeqChain uncounted;
    private AtomicInteger last_acked_seq;
    private AtomicBoolean at = new AtomicBoolean(true);

    public Examiner(int maxcontrol, int maxdata, int seq){
        this.control = new LinkedBlockingQueue<>(maxcontrol);
        this.uncounted =  new SimpleSeqChain(maxdata);
        this.last_acked_seq = new AtomicInteger(seq);
    }

    public void supply(Packet packet) throws InterruptedException, NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();
            
        if(packet instanceof DataPacket) {
            DataPacket dp = (DataPacket) packet;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("flag " + dp.getFlag());
            System.out.println("seq " + dp.getSeq());
            System.out.println("timestamp " + dp.getTimestamp());
            System.out.println("streamid " + dp.getMessageNumber());

        }else{
            ControlPacket cp = (ControlPacket)packet;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("type " + cp.getType());
            System.out.println("extcode " + cp.getExtendedtype());
            System.out.println("timestamp " + cp.getTimestamp());
        }

        if( packet instanceof ControlPacket)
            this.control((ControlPacket)packet);

        else if( packet instanceof DataPacket)
            this.data((DataPacket)packet);

    }

    public int getLastAck(){
        return last_acked_seq.get();
    }

    public void incAck() throws NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();

        last_acked_seq.incrementAndGet();
    }

    void data(DataPacket packet) throws InterruptedException, NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();

        uncounted.add(packet);
    }

    void control(ControlPacket packet) throws InterruptedException, NotActiveException{
        if( !this.at.get() )
            throw new NotActiveException();

        control.put(packet);
    }

    public void terminate(){
        this.at.set(false);

        control.clear();
        uncounted.clear();
    }
}
