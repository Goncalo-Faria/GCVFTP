package Transport.Sender;


import Transport.ControlPacketTypes.*;
import Transport.TransmissionTransportChannel;
import Transport.Unit.DataPacket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SendGate {

    //int msb = (m & 0xff) >> 7;

    private Accountant send_buffer;

    private TransmissionTransportChannel ch;

    private SendWorker worker;

    private AtomicInteger backery_ticket = new AtomicInteger(0);

    private SenderProperties properties;


    /* receiver
     * manda ok e avisa o port
     * fica à escuta de dados
     * espera ack2
     * */

    /* sender
     * manda ack2
     * manda dados
     * */

    public SendGate(SenderProperties me, TransmissionTransportChannel ch, int our_seq, long initialperiod) throws IOException {
        System.out.println("SendGate created");
        this.ch = ch;
        this.properties = me;
        this.send_buffer = new Accountant(me.transmissionChannelBufferSize(),our_seq);
        this.worker = new SendWorker(ch, send_buffer, initialperiod, me);
    }

    public void confirmHandshake() throws  IOException{
        this.properties.window().sentTransmission();
        this.ch.sendPacket( new SURE(SURE.ack_hi,this.connection_time()));
    }

    public void sendSure(int ack) throws  IOException{
        this.properties.window().sentTransmission();
        this.properties.window().setLastReceivedOk(ack);
        this.ch.sendPacket( new SURE(SURE.ack_ok,this.connection_time(),ack));

    }

    public SenderProperties properties(){
        return this.properties;
    }

    public void sendBye( short extcode ) throws IOException{
        this.properties.window().sentTransmission();
        this.ch.sendPacket(new BYE(extcode,
                this.connection_time()));
        System.out.println("BYE");
    }

    public void sendSup(short extcode) throws IOException{
        this.properties.window().sentTransmission();
        this.ch.sendPacket(new SUP(extcode,
                this.connection_time()));
    }
    
    public OK sendOk(short extcode, int last_seq,int free_window) throws IOException{

        this.properties.window().sentTransmission();

        OK packet = new OK(extcode,
                this.connection_time(),
                last_seq,
                free_window,
                this.properties.window().rtt(),
                this.properties.window().rttVar());

        this.ch.sendPacket(packet);

        return packet;
    }

    public void sendForgetit(short extcode)throws IOException {
        this.properties.window().sentTransmission();
        this.ch.sendPacket( new FORGETIT(extcode, this.connection_time()) );
    }

    public void sendNack(List<Integer> losslist ) throws IOException{
        this.properties.window().sentTransmission();
        if( !losslist.isEmpty() ) {
            this.ch.sendPacket(
                    new NOPE((short) 0,
                            this.connection_time(), losslist));
            System.out.println("SENT NACK REAL");
        }

    }

    public void gotok(int seq) throws InterruptedException, NotActiveException {
        this.send_buffer.ok(seq);
    }

    public void gotnack( List<Integer> losslist ) throws NotActiveException, InterruptedException{
        this.gotok(losslist.get(0)-1);
        this.send_buffer.nope(losslist);
    }

    public int connection_time(){
        return this.properties.window().connectionTime();
    }

    public void send( byte[] data) throws IOException, InterruptedException{

        int ticket = this.getTicket();

        DataPacket packet = new DataPacket(
                data,
                this.connection_time(),
                ticket,
                DataPacket.Flag.SOLO);

        this.send_buffer.data(packet);

    }

    private int getTicket(){
        return backery_ticket.accumulateAndGet(0,
                (x,y) -> Integer.max(++x % Integer.MAX_VALUE, y)
        );
    }

    public void send(InputStream io) throws InterruptedException, IOException{
        /* encrava */
        int ticket = this.getTicket();
        BufferedInputStream bufst = new BufferedInputStream(io);
        int flag;

        byte[] data = new byte[this.ch.inMTU()];

        do {
            flag = bufst.read(data, 0, this.ch.inMTU());

            if (flag != -1) {

                DataPacket dp = new DataPacket(
                        data,
                        flag,
                        this.connection_time(),
                        ticket,
                        DataPacket.Flag.SOLO);

                this.send_buffer.data( dp);
            }
        }while ( flag != -1 );

        DataPacket dp = new DataPacket(
                new byte[0],
                0,
                this.connection_time(),
                ticket,
                DataPacket.Flag.LAST);

        this.send_buffer.data( dp);
        /* desencrava*/
    }

    public int getLastOk(){
        return this.send_buffer.lastOk();
    }

    public void close() throws IOException{
        System.out.println("SendGate closed");
        this.worker.stop();
        this.send_buffer.terminate();

    }

}
