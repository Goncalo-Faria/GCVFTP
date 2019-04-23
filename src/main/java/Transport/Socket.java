package Transport;

import Transport.ControlPacketTypes.HI;
import Transport.ControlPacketTypes.SURE;
import Transport.Sender.SendGate;
import Transport.Receiver.ReceiveGate;
import Transport.Start.GCVListener;
import Transport.Sender.SenderProperties;
import Transport.Receiver.ReceiverProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

public class Socket {

    private final long initial_sending_period = GCVConnection.rate_control_interval;

    private SendGate sgate ;
    private ReceiveGate rgate;
    private AtomicBoolean persistent = new AtomicBoolean(true);
    private Thread[] workers;

    private Executor actuary;

    private TransmissionTransportChannel channel ;


    public Socket(SenderProperties me, ReceiverProperties caller, int their_seq) throws IOException {
        this(me,caller,their_seq,1);
    }

    public Socket(SenderProperties me, ReceiverProperties caller, int their_seq, int num_executors) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                me,
                caller);

        HI hello_packet = new HI(
                (short)0,
                0 ,
                this.channel.getSelfStationProperties().packetsize(),
                me.window().getMaxWindow()
        );

        this.channel.sendPacket(hello_packet);
        this.sgate = new SendGate(me,channel,hello_packet.getSeq(),initial_sending_period);

        this.rgate = new ReceiveGate(caller, channel, their_seq);
        this.actuary = new Executor(sgate, rgate, their_seq, hello_packet.getSeq());

        this.workers = new Thread[num_executors];
        
        for( int i = 0; i < num_executors; i++){
            this.workers[i] = new Thread(this.actuary);
            this.workers[i].start();
        }
    }

    public Socket(DatagramSocket in, SenderProperties me, ReceiverProperties caller, int their_seq, int our_seq) throws IOException {
        this(in,me,caller,their_seq,our_seq,1);
    }

    public Socket(DatagramSocket in, SenderProperties me, ReceiverProperties caller, int their_seq, int our_seq, int num_executors) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                in,
                me,
                caller);

        this.sgate = new SendGate(me,channel,our_seq,initial_sending_period);
        this.rgate = new ReceiveGate(caller,channel,their_seq);

        this.sgate.confirmHandshake();

        this.actuary = new Executor(sgate, rgate,their_seq,our_seq);

        this.workers = new Thread[num_executors];

        for( int i = 0; i < num_executors; i++){
            this.workers[i] = new Thread(this.actuary);
            this.workers[i].start();
        }

    }

    public void close( short code ) throws IOException{
        System.out.println("Socket closed");
        
        this.actuary.terminate();

        GCVListener.closeConnection(
                this.channel.getOtherStationProperties().ip().toString() + this.channel.getOtherStationProperties().port());

        this.sgate.sendBye(code);

        this.sgate.close();

        this.rgate.close();

        this.channel.close();

    }

    public void send( byte[] data) throws IOException, InterruptedException {
        this.sgate.send(data);
    }

    public void send( InputStream io ) throws  IOException, InterruptedException{
        this.sgate.send(io);
    }

    public OutputStream send() throws  IOException, InterruptedException{
        PipedOutputStream producer = new PipedOutputStream();
        PipedInputStream consumer = new PipedInputStream(producer);
        this.sgate.send(consumer);
        return producer;
    }

    public InputStream receive() throws InterruptedException {
        return this.actuary.getStream();
    }

}
