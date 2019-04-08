package Transport;


import Transport.ControlPacketTypes.BYE;
import Transport.ControlPacketTypes.HI;
import Transport.ControlPacketTypes.SURE;
import Transport.Start.GCVListener;
import Transport.Start.SenderProperties;
import Transport.Start.TransportStationProperties;

import Transport.Unit.ControlPacket;
import Transport.Unit.DataPacket;
import Transport.Unit.Packet;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.DatagramSocket;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

public class Socket {

    //int msb = (m & 0xff) >> 7;

    private Accountant send_buffer;
    private LocalDateTime connection_start_time = LocalDateTime.now();
    private TransmissionTransportChannel ch;
    private AtomicInteger seq;
    private Sender worker;
    private AtomicInteger backery_ticket = new AtomicInteger(0);

    private long initialperiod = 100;

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
        this.seq = new AtomicInteger(seq);
        send_buffer = new Accountant(me.getStock());

        this.handshake();

        this.worker = new Sender(ch, send_buffer,initialperiod,me);
    }

    public Socket(DatagramSocket in, SenderProperties me,TransportStationProperties caller, int seq) throws IOException {
        System.out.println("Socket created");
        this.ch = new TransmissionTransportChannel( in, me, caller);
        this.seq = new AtomicInteger(seq);
        this.send_buffer = new Accountant(me.getStock());

        this.ch.sendPacket( new SURE(SURE.ack_hi,this.connection_time()));

        this.worker = new Sender(ch, send_buffer,initialperiod,me);
    }

    public void handshake() throws IOException{
        this.ch.sendPacket(new HI((short)0,
                        this.connection_time() ,
                        this.ch.getSelfStationProperties().packetsize(),
                        this.ch.getSelfStationProperties().window()));

    }

    private int connection_time(){
        return (int)this.connection_start_time.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    }

    public void send( byte[] data) throws IOException, InterruptedException{

        int ticket = this.getTicket();

        int sequence_no = this.seq();

        DataPacket packet = new DataPacket(
                data,
                this.connection_time(),
                sequence_no,
                ticket,
                DataPacket.Flag.SOLO);

        this.send_buffer.data( sequence_no, packet);

        this.send_buffer.finish(ticket);/*espera que seja tudo enviado e confirmado*/

    }

    private int getTicket(){
        return backery_ticket.accumulateAndGet(0,
                (x,y) -> Integer.max(++x % Integer.MAX_VALUE, y)
        );
    }

    private int seq(){
        return this.seq.accumulateAndGet(0,
                (x,y) -> Integer.max(++x % Integer.MAX_VALUE, y)
        );
    }

    public void send(InputStream io){
        /* encrava */
        int ticket = this.getTicket();
        BufferedInputStream bufst = new BufferedInputStream(io);
        int flag;

        try {
            byte[] data = new byte[this.ch.inMTU()];

            do {
                flag = bufst.read(data, 0, this.ch.inMTU());

                if (flag != -1) {
                    int sequence_n = this.seq();
                    DataPacket dp = new DataPacket(
                            data,
                            flag,
                            this.connection_time(),
                            sequence_n,
                            ticket,
                            DataPacket.Flag.SOLO);

                    this.send_buffer.data(sequence_n, dp);
                }
            }while ( flag != -1 );

            int sequence_n = this.seq();

            DataPacket dp = new DataPacket(
                    new byte[0],
                    0,
                    this.connection_time(),
                    sequence_n,
                    ticket,
                    DataPacket.Flag.LAST);

            this.send_buffer.data(sequence_n, dp);

            this.send_buffer.finish(ticket);/*espera que seja tudo enviado e confirmado*/

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
        }else{
            ControlPacket cp = (ControlPacket)p;
            System.out.println("x-----------x-----------x--------x-------x----x--x--x-x-x-x--x ");
            System.out.println("type " + cp.getType());
            System.out.println("extcode " + cp.getExtendedtype());
            System.out.println("timestamp " + cp.getTimestamp());
        }

        return new byte[0];
    }

    public void close( short code ) throws IOException{
        this.ch.sendPacket(new BYE(code,
                this.connection_time()));

        this.ch.close();
        this.worker.stop();
        this.send_buffer.terminate();

        GCVListener.closeConnection(
                this.ch.getOtherStationProperties().ip().toString() + this.ch.getOtherStationProperties().port());
    }

    public void  close() throws IOException{
        this.close((short)0);
    }

}
