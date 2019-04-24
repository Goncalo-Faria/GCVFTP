package Transport;

import Transport.ControlPacketTypes.HI;
import Transport.Sender.SendGate;
import Transport.Receiver.ReceiveGate;
import Transport.Start.GCVListener;
import Transport.Sender.SenderProperties;
import Transport.Receiver.ReceiverProperties;

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
    private Thread worker;
    private Executor actuary;
    private int ourseq;


    private TransmissionTransportChannel channel ;

    public Socket(SenderProperties me, ReceiverProperties caller, int their_seq) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                me,
                caller);

        HI hello_packet = new HI(
                (short)0,
                0 ,
                this.channel.getSelfStationProperties().mtu(),
                me.window().getMaxWindowSize()
        );

        this.ourseq = hello_packet.getSeq();

        this.channel.sendPacket( hello_packet);

        this.boot(me, caller, their_seq, this.ourseq);
    }

    public Socket(DatagramSocket in, SenderProperties me, ReceiverProperties caller, int their_seq, int our_seq) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                in,
                me,
                caller);

        this.ourseq = our_seq;

        this.boot(me, caller, their_seq, our_seq);
    }

    void boot(SenderProperties me, ReceiverProperties caller, int their_seq, int our_seq) throws IOException{
        this.sgate = new SendGate(me,channel,our_seq,initial_sending_period);
        this.rgate = new ReceiveGate(caller,channel,their_seq);

        this.sgate.confirmHandshake();

        me.window().setLastSentOk(their_seq);
        me.window().setLastReceivedOk(our_seq);

        this.actuary = new Executor(sgate, rgate, me.window() );

        this.worker = new Thread(this.actuary);

        this.worker.start();
    }

    public void close() throws IOException{
        System.out.println("Socket closed");
        if( !this.actuary.hasTerminated() )
            this.actuary.terminate((short)0);

        GCVListener.closeConnection(
                    this.channel.getOtherStationProperties().ip().toString()
                            + this.channel.getOtherStationProperties().port());

        this.channel.close();

    }

    public void send( byte[] data) throws IOException, InterruptedException {
        if(this.actuary.hasTerminated())
            throw new IOException("Socket has disconnected");

        this.sgate.send(data);
    }

    public void send( InputStream io ) throws  IOException, InterruptedException{
        if(this.actuary.hasTerminated())
            throw new IOException("Socket has disconnected");

        this.sgate.send(io);
    }

    public OutputStream send() throws  IOException, InterruptedException{
        if(this.actuary.hasTerminated())
            throw new IOException("Socket has disconnected");

        PipedOutputStream producer = new PipedOutputStream();
        PipedInputStream consumer = new PipedInputStream(producer);
        this.sgate.send(consumer);
        return producer;
    }

    public InputStream receive() throws InterruptedException {
        return this.actuary.getStream();
    }


    public void restart() throws IOException {
        this.channel.sendPacket( new HI(
                (short)0,
                this.sgate.connection_time(),
                this.channel.getSelfStationProperties().mtu(),
                this.sgate.properties().window().getMaxWindowSize()
        ));
    }

}
