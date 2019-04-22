package Transport.Receiver;

import java.time.LocalDateTime;
import java.util.List;

import Transport.TransmissionTransportChannel;
import Transport.Receiver.ReceiveWorker;
import Transport.Unit.*;

public class ReceiveGate {

    private Examiner receive_buffer;

    private LocalDateTime connection_start_time = LocalDateTime.now();

    private TransmissionTransportChannel channel;

    private ReceiveWorker worker;

    private ReceiverProperties properties;

    public ReceiveGate(ReceiverProperties me, TransmissionTransportChannel ch, int seq){
        System.out.println("ReceiveGate created");
        this.properties = me;
        this.channel = ch;
        this.receive_buffer = new Examiner(
            (int)(0.3 * me.transmissionchannel_buffer_size()),
            (int)(0.7 * me.transmissionchannel_buffer_size()),
            seq
        );

        this.worker = new ReceiveWorker(channel, receive_buffer, me);
        
    }

    public int getLastSeq(){
        return receive_buffer.getLastAck();
    }

    public List<Integer> getLossList(){
        return receive_buffer.getLossList();
    }

    public ControlPacket control() throws InterruptedException{
        return this.receive_buffer.getControlPacket();
    }

    public DataPacket data() throws InterruptedException{
        return this.receive_buffer.getDataPacket();
    }

    public void close(){
        System.out.println("ReceiveGate closed");
        this.worker.stop();
        this.receive_buffer.terminate();
    }
    
}
