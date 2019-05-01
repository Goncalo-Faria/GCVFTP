package Transport.Sender;


import Test.Debugger;
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
        Debugger.log("SendGate created");
        this.ch = ch;
        this.properties = me;
        this.send_buffer = new Accountant(me.transmissionChannelBufferSize(),our_seq);
        this.worker = new SendWorker(ch, send_buffer, initialperiod, me);
    }

    public void confirmHandshake() throws  IOException{

        this.ch.sendPacket( new SURE(SURE.ack_hi,
                this.properties.window().connectionTime()));
    }

    public void sendSure(int ack) throws  IOException{

        this.ch.sendPacket(
                new SURE(
                        SURE.ack_ok,
                        this.properties.window().connectionTime(),
                        ack
                )
        );

    }

    public SenderProperties properties(){
        return this.properties;
    }

    public void sendBye( short extcode ) throws IOException{

        this.ch.sendPacket(
                new BYE(
                        extcode,
                        this.properties.window().connectionTime()
                )
        );
        Debugger.log("BYE");
    }

    public void sendSup(short extcode) throws IOException{

        this.ch.sendPacket(
                new SUP(
                        extcode,
                        this.properties.window().connectionTime()
                )
        );
    }
    
    public OK sendOk(short extcode, int last_seq, int free_window) throws IOException{

        OK packet = new OK(
                extcode,
                this.properties.window().connectionTime(),
                last_seq,
                free_window,
                this.properties.window().rtt(),
                this.properties.window().rttVar()
        );

        this.ch.sendPacket(packet);

        return packet;
    }

    public void sendForgetit(short extcode)throws IOException {

        this.ch.sendPacket(
                new FORGETIT(
                        extcode,
                        this.properties.window().connectionTime()
                )
        );
    }

    public void sendNope( List<Integer> lossList ) throws IOException{

        if( !lossList.isEmpty() ) {
            this.ch.sendPacket(
                    new NOPE(
                            (short) 0,
                            this.properties.window().connectionTime(),
                            lossList
                    )
            );

            Debugger.log("SENT NACK " + lossList.get(0) );
        }

    }

    public void release(int seq) throws InterruptedException, NotActiveException {
        this.send_buffer.ok(seq);
    }

    public void retransmit(List<Integer> lossList ) throws NotActiveException, InterruptedException{
        Debugger.log(">>>>>>>>>>><Retransmitting : " + lossList.size()+ " <<<<<<<<<<<<<<<<<<");
        this.send_buffer.nope(lossList);
    }

    public void retransmit(){
        this.send_buffer.retransmit();
    }

    public void send( byte[] data) throws IOException, InterruptedException{

        int ticket = this.getTicket();

        DataPacket packet = new DataPacket(
                data,
                this.properties.window().connectionTime(),
                ticket,
                DataPacket.Flag.SOLO
        );

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
                        this.properties.window().connectionTime(),
                        ticket,
                        DataPacket.Flag.SOLO
                );

                this.send_buffer.data( dp);
            }
        }while ( flag != -1 );

        DataPacket dp = new DataPacket(
                new byte[0],
                0,
                this.properties.window().connectionTime(),
                ticket,
                DataPacket.Flag.LAST
        );

        this.send_buffer.data( dp);
        /* desencrava*/
    }

    public void close() throws IOException{
        Debugger.log("SendGate closed");
        this.worker.stop();
        this.send_buffer.terminate();

    }

}
