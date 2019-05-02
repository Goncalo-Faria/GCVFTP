package Transport.Sender;


import Test.Debugger;
import Transport.TransportChannel;
import Transport.Unit.ControlPacketTypes.*;
import Transport.Impl.TransmissionTransportChannel;
import Transport.Unit.DataPacket;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.NotActiveException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SendGate {

    //int msb = (m & 0xff) >> 7;

    private final Accountant send_buffer;

    private final TransportChannel ch;

    private final SendWorker worker;

    private final AtomicInteger backery_ticket = new AtomicInteger(0);

    private final SenderProperties properties;


    /* receiver
     * manda ok e avisa o port
     * fica à escuta de dados
     * espera ack2
     * */

    /* sender
     * manda ack2
     * manda dados
     * */

    public SendGate(SenderProperties me, TransmissionTransportChannel ch, int our_seq, long initialperiod) {
        Debugger.log("SendGate created");
        this.ch = ch;
        this.properties = me;
        this.send_buffer = new Accountant(me.transmissionChannelBufferSize(), our_seq);
        this.worker = new SendWorker(ch, send_buffer, initialperiod, me);
    }

    public void confirmHandshake() throws IOException {

        this.ch.sendPacket(new SURE(SURE.ack_hi,
                this.ch.window().connectionTime()));
    }

    public void sendSure(int ack) throws IOException {

        this.ch.sendPacket(
                new SURE(
                        SURE.ack_ok,
                        this.ch.window().connectionTime(),
                        ack
                )
        );

    }

    public SenderProperties properties() {
        return this.properties;
    }

    public void sendBye(short extcode) throws IOException {

        this.ch.sendPacket(
                new BYE(
                        extcode,
                        this.ch.window().connectionTime()
                )
        );
        Debugger.log("BYE");
    }

    public void sendSup(short extcode) throws IOException {

        this.ch.sendPacket(
                new SUP(
                        extcode,
                        this.ch.window().connectionTime()
                )
        );
    }

    public void sendOk(short extcode, int last_seq, int free_window) throws IOException {

        OK packet = new OK(
                extcode,
                this.ch.window().connectionTime(),
                last_seq,
                free_window,
                this.ch.window().rtt(),
                this.ch.window().rttVar()
        );

        this.ch.sendPacket(packet);

    }

    public void sendForgetit(short extcode) throws IOException {

        this.ch.sendPacket(
                new FORGETIT(
                        extcode,
                        this.ch.window().connectionTime()
                )
        );
    }

    public void sendNope(List<Integer> lossList) throws IOException {

        if (!lossList.isEmpty()) {
            this.ch.sendPacket(
                    new NOPE(
                            (short) 0,
                            this.ch.window().connectionTime(),
                            lossList
                    )
            );

            Debugger.log("SENT NACK " + lossList.get(0));
        }

    }

    public void release(int seq) throws InterruptedException, NotActiveException {
        this.send_buffer.ok(seq);
    }

    public void retransmit(List<Integer> lossList) throws NotActiveException, InterruptedException {
        Debugger.log(">>>>>>>>>>><Retransmitting : " + lossList.size() + " <<<<<<<<<<<<<<<<<<");
        this.send_buffer.nope(lossList);
    }

    public void retransmit() {
        this.send_buffer.retransmit();
    }

    public void send(byte[] data) throws InterruptedException {

        int ticket = this.getTicket();

        DataPacket packet = new DataPacket(
                data,
                ticket,
                DataPacket.Flag.SOLO
        );

        this.send_buffer.data(packet);
    }

    private int getTicket() {
        return backery_ticket.accumulateAndGet(0,
                (x, y) -> Integer.max(++x % Integer.MAX_VALUE, y)
        );
    }

    public void send(InputStream io) {

        Thread copy_machine = new Thread(
                new LoadingWorker(
                        this.getTicket(),
                        io,
                        this.send_buffer,this.ch)
        );

        copy_machine.start();
    }

    public void sendWhenReady(InputStream io) throws InterruptedException{

        Thread copyMachine = new Thread(
                new LoadingWorker(
                        this.getTicket(),
                        io,
                        this.send_buffer,this.ch)
        );

        copyMachine.start();
        copyMachine.join();
    }

    public void close() {
        Debugger.log("SendGate closed");
        this.worker.stop();
        this.send_buffer.terminate();
    }

    class LoadingWorker implements Runnable {
        private int ticket;
        private InputStream io;
        private Accountant send_buffer;
        private TransportChannel ch;

        LoadingWorker(int ticket, InputStream io, Accountant send_buffer, TransportChannel ch){
            this.ticket = ticket;
            this.io = io;
            this.send_buffer = send_buffer;
            this.ch = ch;
        }

        public void run(){
            //BufferedInputStream bufst = new BufferedInputStream(io);
            int flag;
            boolean first = true;

            byte[] data = new byte[this.ch.inMTU()];
            try {
                do {
                    flag = io.read(data, 0, this.ch.inMTU());
                    if (flag != -1) {
                        DataPacket dp;
                        if (first) {
                            dp = new DataPacket(
                                    data,
                                    flag,
                                    ticket,
                                    DataPacket.Flag.FIRST
                            );
                            first = false;
                        } else {
                            dp = new DataPacket(
                                    data,
                                    flag,
                                    ticket,
                                    DataPacket.Flag.MIDDLE
                            );
                        }
                        this.send_buffer.data(dp);
                    }

                } while (flag != -1);

                DataPacket dp = new DataPacket(
                        new byte[0],
                        0,
                        ticket,
                        DataPacket.Flag.LAST
                );

                this.send_buffer.data(dp);
            }catch (InterruptedException|IOException e){
                e.printStackTrace();
            }
        }
    }

}