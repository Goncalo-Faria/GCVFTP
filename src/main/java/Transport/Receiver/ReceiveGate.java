package Transport.Receiver;

import java.util.List;

import Transport.GCVConnection;
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
            (int)(GCVConnection.controlBufferFactor * me.transmissionChannelBufferSize()),
            me.transmissionChannelBufferSize(),
            seq
        );

        this.worker = new ReceiveWorker(channel, receiveBuffer, me, GCVConnection.number_of_receive_workers);
        
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

    public void prepareRetransmition(){
        this.receiveBuffer.clear();
    }
    
}
