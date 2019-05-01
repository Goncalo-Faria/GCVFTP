package Transport.Receiver;

import java.util.List;

import Test.Debugger;
import Transport.GCVConnection;
import Transport.TransportChannel;
import Transport.Unit.*;

public class ReceiveGate {

    private Examiner receiveBuffer;

    private ReceiveWorker worker;

    private ReceiverProperties properties;

    public ReceiveGate(ReceiverProperties me, TransportChannel ch, int seq){
        Debugger.log("ReceiveGate created");
        this.properties = me;
        this.receiveBuffer = new Examiner(
            (int)(GCVConnection.controlBufferFactor * me.transmissionChannelBufferSize()),
            me.transmissionChannelBufferSize(),
            seq
        );

        this.worker = new ReceiveWorker(ch, receiveBuffer, me, GCVConnection.number_of_receive_workers);
        
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
        Debugger.log("ReceiveGate closed");
        this.worker.stop();
        this.receiveBuffer.terminate();
    }

    public void prepareRetransmition(){
        this.receiveBuffer.clear();
    }
    
}
