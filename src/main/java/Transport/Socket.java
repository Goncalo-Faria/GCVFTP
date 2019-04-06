package Transport;

import AgenteUDP.StationProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.PriorityBlockingQueue;

public class Socket {

    //int msb = (m & 0xff) >> 7;

    private PriorityBlockingQueue<DataPacket> bag = new PriorityBlockingQueue<>();
    private LocalDateTime connection_start_time = LocalDateTime.now();
    private TransportChannel ch;
    private int seq=1;

    /* receiver
     * manda ack e avisa o port
     * fica à escuta de dados
     * espera ack2
     * */

    /* sender
     * manda ack2
     * manda dados
     * */

    public Socket(StationProperties me, StationProperties caller) throws IOException {
        this.ch = new TransmissionTransportChannel( me, caller);

        this.ch.send( new ControlPacket(ByteBuffer.allocate(4).putInt(me.packetsize()).array(),ControlPacket.Type.HI,0));
       /* deve esperar pelo ack2*/
    }

    public Socket(DatagramSocket in, StationProperties me,StationProperties caller ) throws IOException {
        this.ch = new TransmissionTransportChannel( in, me, caller);
        //this.ch.send( ControlPacket.hi(this.connection_time()));/*ack2*/

    }

    private int connection_time(){
        return (int)this.connection_start_time.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    }

    public void send( byte[] data) throws IOException{
        DataPacket packet = new DataPacket(data, this.connection_time(),this.seq++,1, DataPacket.Flag.SOLO);
        this.ch.send( packet );
    }


    public byte[] receive() throws IOException{
        Packet p = this.ch.receive();
        if(p instanceof DataPacket) {
            DataPacket dp = (DataPacket) p;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("flag " + dp.getFlag());
            System.out.println("seq " + dp.getSeq());
            System.out.println("timestamp " + dp.getTimestamp());
            System.out.println("window " + dp.getWindow());
            return dp.getData();
        }

        return new byte[0];
    }
}
