package Transport;

import Transport.Sender.SendGate;
import Transport.Start.GCVListener;
import Transport.Sender.SenderProperties;
import Transport.Start.TransportStationProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;

public class Socket {

    private long initial_sending_period = 100;

    private SendGate ingate ;
    private TransmissionTransportChannel channel ;


    public Socket(SenderProperties me, TransportStationProperties caller, int seq) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                me,
                caller);

        this.ingate = new SendGate(me,channel,seq,initial_sending_period);

        this.ingate.handshake();
    }

    public Socket(DatagramSocket in, SenderProperties me, TransportStationProperties caller, int seq) throws IOException {
        System.out.println("Socket created");
        this.channel = new TransmissionTransportChannel(
                in,
                me,
                caller);

        this.ingate = new SendGate(me,channel,seq,initial_sending_period);

        this.ingate.confirm_handshake();
    }

    public void close( short code ) throws IOException{

        GCVListener.closeConnection(
                this.channel.getOtherStationProperties().ip().toString() + this.channel.getOtherStationProperties().port());

        this.ingate.bye(code);

        this.ingate.close();

        this.channel.close();

    }

    public void send( byte[] data) throws IOException, InterruptedException {
        this.ingate.send(data);
    }

    public void send( InputStream io ) throws  IOException, InterruptedException{
        this.ingate.send(io);
    }

    public byte[] receive() throws IOException {
        Packet p = this.channel.receivePacket();
        if(p instanceof DataPacket) {
            DataPacket dp = (DataPacket) p;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("flag " + dp.getFlag());
            System.out.println("seq " + dp.getSeq());
            System.out.println("timestamp " + dp.getTimestamp());
            System.out.println("streamid " + dp.getMessageNumber());
            return dp.getData();
        }else{
            ControlPacket cp = (ControlPacket)p;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("type " + cp.getType());
            System.out.println("extcode " + cp.getExtendedtype());
            System.out.println("timestamp " + cp.getTimestamp());
        }

        return new byte[0];
    }

    public void restart() throws IOException{
        this.ingate.handshake();
    }

}
