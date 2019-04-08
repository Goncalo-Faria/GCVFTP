package Transport;

import AgenteUDP.StationProperties;
import Transport.ControlPacketTypes.HI;
import Transport.Start.SenderProperties;
import Transport.Start.TransportStationProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;

import javax.xml.crypto.Data;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Timer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.PriorityBlockingQueue;

public class Socket {

    //int msb = (m & 0xff) >> 7;

    private Accountant<Packet> send_buffer;
    private LocalDateTime connection_start_time = LocalDateTime.now();
    private TransmissionTransportChannel ch;
    private int seq;
    private Sender worker;

    /* receiver
     * manda ack e avisa o port
     * fica à escuta de dados
     * espera ack2
     * */

    /* sender
     * manda ack2
     * manda dados
     * */

    public Socket(SenderProperties me, TransportStationProperties caller, int seq) throws IOException {
        System.out.println("Socket created");
        this.ch = new TransmissionTransportChannel( me, caller);
        this.ch.sendPacket( new HI((short)0,0 , me.packetsize(), me.window()));
        System.out.println("Sender created");
        this.seq = seq;
        send_buffer = new Accountant<Packet>(me.getStock(), me.getLevel());
        this.worker = new Sender(ch, send_buffer,100,me);
        /* deve esperar pelo ack2*/
    }

    public Socket(DatagramSocket in, SenderProperties me,TransportStationProperties caller, int seq) throws IOException {
        this.ch = new TransmissionTransportChannel( in, me, caller);
        this.seq = seq;
        send_buffer = new Accountant<Packet>(me.getStock(),me.getLevel());
        this.worker = new Sender(ch, send_buffer,100,me);

        //this.ch.send( ControlPacket.hi(this.connection_time()));/*ack2*/

    }

    private int connection_time(){
        return (int)this.connection_start_time.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    }

    public void send( byte[] data) throws IOException{
        DataPacket packet = new DataPacket(data, this.connection_time(),this.increment(),1, DataPacket.Flag.SOLO);
        this.ch.sendPacket( packet );
    }

    public void send(InputStream io){
        /* encrava */

        BufferedInputStream bufst = new BufferedInputStream(io);

        try {
            byte[] data = new byte[this.ch.inMTU()];

            while (true) {
                this.send_buffer.await();
                int flag = bufst.read(data, 0, this.ch.inMTU());

                if(flag == -1){
                    throw new IOException();
                }

                DataPacket dp = new DataPacket(
                        data,
                        flag,
                        this.connection_time(),
                        this.increment(),
                        1,
                        DataPacket.Flag.SOLO);

                this.send_buffer.data(this.seq, dp);
            }
        }catch(IOException e){

        }catch(InterruptedException e){
            e.printStackTrace();
        }

        /* desencrava*/
    }


    public byte[] receive() throws IOException{
        Packet p = this.ch.receivePacket();
        if(p instanceof DataPacket) {
            DataPacket dp = (DataPacket) p;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("flag " + dp.getFlag());
            System.out.println("seq " + dp.getSeq());
            System.out.println("timestamp " + dp.getTimestamp());
            System.out.println("message number " + dp.getMessageNumber());
            return dp.getData();
        }

        return new byte[0];
    }

    private int increment(){
        return (++this.seq )%Integer.MAX_VALUE;
    }
}
