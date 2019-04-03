package Transport;

import AgenteUDP.Channel;
import AgenteUDP.StationProperties;
import AgenteUDP.StreamIN;
import AgenteUDP.TransmissionChannel;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.PriorityBlockingQueue;

public class Socket implements Channel{

    //int msb = (m & 0xff) >> 7;

    private PriorityBlockingQueue<DataPacket> bag = new PriorityBlockingQueue<>();
    private LocalDateTime connection_start_time = LocalDateTime.now();
    private Channel ch;

    /* receiver
     * manda ack e avisa o port
     * fica à escuta de dados
     * espera ack2
     * */

    /* sender
     * manda ack2
     * manda dados
     * */

    public Socket(StationProperties me, StationProperties caller) throws SocketException,InterruptedException {
        this.ch = new TransmissionChannel( me, caller);

        ControlPacket ackpacket = new ControlPacket(
                ByteBuffer.allocate(4).putInt(me.port()).array(),
                ControlPacket.Type.OK,
                this.connection_time());

        this.ch.send(ackpacket.serialize());/* ack w/ port */

       /* deve esperar pelo ack2*/
    }

    public Socket(StreamIN in, StationProperties caller ) throws SocketException,InterruptedException {
        this.ch = new TransmissionChannel( in, caller);
        this.ch.send( ControlPacket.sure(this.connection_time()).serialize() );/*ack2*/

    }

    private int connection_time(){
        return (int)this.connection_start_time.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    }

    public void send( byte[] data) throws InterruptedException{
        this.ch.send(data);
    }

    public byte[] receive() throws InterruptedException{
        return this.ch.receive();
    }
}
