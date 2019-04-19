package Transport;

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
import java.net.DatagramSocket;

public class Socket {

    private long initial_sending_period = 100;

    private SendGate sgate ;
    private ReceiveGate rgate;

    private TransmissionTransportChannel channel ;


    public Socket(SenderProperties me, ReceiverProperties caller, int send_seq) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                me,
                caller);

        this.sgate = new SendGate(me,channel,send_seq,initial_sending_period);
        this.rgate = new ReceiveGate(caller,channel,this.sgate.handshake());

    }

    public Socket(DatagramSocket in, SenderProperties me, ReceiverProperties caller, int send_seq , int receive_seq) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                in,
                me,
                caller);

        this.sgate = new SendGate(me,channel,send_seq,initial_sending_period);
        this.rgate = new ReceiveGate(caller,channel,receive_seq);

        this.sgate.confirm_handshake();
    }

    public void close( short code ) throws IOException{
        System.out.println("Socket closed");
        GCVListener.closeConnection(
                this.channel.getOtherStationProperties().ip().toString() + this.channel.getOtherStationProperties().port());

        this.sgate.bye(code);

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

    public byte[] receive() throws IOException {
        return new byte[0];
    }

    public void restart() throws IOException{
        this.sgate.handshake();
    }

}
