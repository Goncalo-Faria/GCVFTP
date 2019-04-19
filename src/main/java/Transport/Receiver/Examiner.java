package Transport.Receiver;

import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.util.concurrent.LinkedBlockingQueue;

public class Examiner implements Runnable{

    private LinkedBlockingQueue<ControlPacket> control;
    private SimpleSeqChain uncounted;

    public Examiner(int maxcontrol, int maxdata){
        this.control = new LinkedBlockingQueue<>(maxcontrol);
    }

    public void supply(Packet packet) throws InterruptedException{

        if( packet instanceof ControlPacket)
            this.control((ControlPacket)packet);

        else if( packet instanceof DataPacket)
            this.data((DataPacket)packet);

    }

    void data(DataPacket packet) throws InterruptedException{
    }

    void control(ControlPacket packet) throws InterruptedException{
        control.put(packet);
    }

    public void run(){

    }
}
