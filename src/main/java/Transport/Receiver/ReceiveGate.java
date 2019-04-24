package Transport.Receiver;

import java.util.List;

import Transport.TransmissionTransportChannel;
import Transport.Unit.*;

public class ReceiveGate {

    private Examiner receiveBuffer;

    private TransmissionTransportChannel channel;

    private ReceiveWorker worker;

    private ReceiverProperties properties;

    public ReceiveGate(ReceiverProperties me, TransmissionTransportChannel ch, int seq){
        System.out.println("ReceiveGate created");
        this.properties = me;
        this.channel = ch;
        this.receiveBuffer = new Examiner(
            (int)(0.3 * me.transmissionChannelBufferSize()),
            (int)(0.7 * me.transmissionChannelBufferSize()),
            seq
        );

        this.worker = new ReceiveWorker(channel, receiveBuffer, me);
        
    }

    public int getLastSeq(){
        return receiveBuffer.getLastOk();
    }

    public List<Integer> getLossList(){
        return receiveBuffer.getLossList();
    }

    public ControlPacket control() throws InterruptedException{
        return this.receiveBuffer.getControlPacket();
    }

    public DataPacket data() throws InterruptedException{
        return this.receiveBuffer.getDataPacket();
    }

    public int getWindowSize(){
        return this.receiveBuffer.getWindowSize();
    }

    public void close(){
        System.out.println("ReceiveGate closed");
        this.worker.stop();
        this.receiveBuffer.terminate();
    }
    
}
