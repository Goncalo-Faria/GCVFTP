package Transport;

import Test.Debugger;
import Transport.Impl.FlowWindow;
import Transport.Impl.TransmissionTransportChannel;
import Transport.Unit.ControlPacketTypes.HI;
import Transport.Sender.SendGate;
import Transport.Receiver.ReceiveGate;
import Transport.Sender.SenderProperties;
import Transport.Receiver.ReceiverProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GCVSocket {

    private static ConnectionScheduler common_daemon=null;

    private static void activate(){
        if( common_daemon == null){
            try {
                GCVSocket.common_daemon = new ConnectionScheduler(
                        GCVConnection.port,
                        GCVConnection.connection_receive_ttl,
                        ControlPacket.Type.HI,
                        HI.size);

            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private static void announceSocketConnection(String key, GCVSocket cs){
        if( common_daemon != null)
            GCVSocket.common_daemon.announceConnection(key,cs);
    }

    private static void closeSocketConnection(String key){
        if( common_daemon != null)
            GCVSocket.common_daemon.closeConnection(key);
    }

    private static ConnectionScheduler.StampedControlPacket getStampedPacket() throws InterruptedException{
        return GCVSocket.common_daemon.getStamped();
    }

    public static void closeConnection(){
        GCVSocket.common_daemon.close();
        GCVSocket.common_daemon = null;
    }

    private Executor actuary;
    private boolean persistent = true;

    private final AtomicBoolean active = new AtomicBoolean(false);

    private final int mtu = GCVConnection.stdmtu;
    private int maxWindow = GCVConnection.send_buffer_size;
    private InetAddress localhost;

    private TransmissionTransportChannel channel ;

    public GCVSocket() throws IOException {
        Debugger.log("GCVSocket created");
        this.localhost = InetAddress.getLocalHost();
    }

    public GCVSocket(int maxWindow, boolean persistent) throws IOException {
        this.localhost = InetAddress.getLocalHost();
        this.persistent = persistent;
        this.maxWindow = maxWindow;
    }

    private void boot(SenderProperties me, ReceiverProperties caller, int their_seq, int our_seq, int time) throws IOException{
        this.active.set(true);

        SendGate sgate = new SendGate(me,channel,our_seq,GCVConnection.rate_control_interval/1000);
        ReceiveGate rgate = new ReceiveGate(caller,channel,their_seq);

        sgate.confirmHandshake();

        channel.window().boot(their_seq,our_seq, time);

        this.actuary = new Executor(sgate, rgate, channel.window());

        Thread worker = new Thread(this.actuary);

        worker.start();
    }

    public void listen() throws InterruptedException, IOException{

        GCVSocket.activate();

        ConnectionScheduler.StampedControlPacket receivedStampedPacket =
                GCVSocket.getStampedPacket();

        HI hiPacket = (HI)receivedStampedPacket.get();/*waiting for datagram*/

        InetSocketAddress sa = new InetSocketAddress(0);

        FlowWindow channelWindow = new FlowWindow(hiPacket.getMaxWindow());

        SenderProperties senderProp = new SenderProperties(
                this.localhost,
                sa.getPort(),
                this.mtu,
                channelWindow.getMaxWindowSize(),
                persistent);

        ReceiverProperties receiveProp = new ReceiverProperties(
                receivedStampedPacket.ip(),
                receivedStampedPacket.port(),
                hiPacket.getMTU(),
                this.maxWindow);

        this.channel = new TransmissionTransportChannel(
                senderProp ,
                receiveProp,
                channelWindow
        );

        HI responseHiPacket = new HI(
                (short)0,
                channelWindow.connectionTime() ,
                this.channel.getSelfStationProperties().mtu(),
                channelWindow.getMaxWindowSize()
        );


        this.channel.sendPacket( responseHiPacket );

        this.boot(senderProp, receiveProp, hiPacket.getSeq(), responseHiPacket.getSeq(), responseHiPacket.getTimestamp());

        GCVSocket.announceSocketConnection(receivedStampedPacket.ip().toString() + receivedStampedPacket.port(), this);
    }

    public void connect(String ip, int intendedPort) throws IOException, TimeoutException {

        this.connect(InetAddress.getByName(ip), intendedPort);

    }

    public void connect(InetAddress ip, int intendedPort) throws IOException, TimeoutException {

        FlowWindow channelWindow = new FlowWindow(maxWindow);



        HI hiPacket = new HI(
                (short)0,
                channelWindow.connectionTime(),
                mtu,
                maxWindow
        );


        byte[] serializedHiPacket = hiPacket.serialize();

        DatagramPacket responseDatagram = new DatagramPacket(
                new byte[HI.size],
                HI.size);

        DatagramSocket cs = new DatagramSocket(intendedPort);
        cs.setSoTimeout(GCVConnection.request_retry_timeout);

        for(int tries = 0; tries < GCVConnection.request_retry_number; tries++ ) {

            Debugger.log("sent " + serializedHiPacket.length + " bytes");
            cs.send(new DatagramPacket(
                        serializedHiPacket,
                        0,
                        serializedHiPacket.length,
                        ip,
                        GCVConnection.port)
            );
            try {
                Debugger.log(":localport " + cs.getLocalPort() );
                cs.receive(responseDatagram);
                Packet du = Packet.parse(responseDatagram.getData());

                if(du instanceof ControlPacket){
                    ControlPacket cdu = (ControlPacket)du;

                    if( cdu instanceof HI ){
                        HI response_hello_packet = (HI)cdu;
                        ReceiverProperties receiveProp= new ReceiverProperties(
                                ip,
                                responseDatagram.getPort(),
                                response_hello_packet.getMTU(),
                                this.maxWindow
                        );

                        SenderProperties sendProp = new SenderProperties(
                                InetAddress.getLocalHost(),
                                intendedPort,
                                mtu,
                                response_hello_packet.getMaxWindow(),
                                persistent);

                        this.channel = new TransmissionTransportChannel(cs,
                                sendProp ,
                                receiveProp,
                                channelWindow
                        );

                        this.boot(sendProp,receiveProp, response_hello_packet.getSeq(), hiPacket.getSeq(), response_hello_packet.getTimestamp());
                        return;
                    }

                }

            }catch (SocketTimeoutException ste){
            }
        }

        throw new TimeoutException();

    }

    public void close() throws IOException{
        Debugger.log("GCVSocket closed");
        if( !this.actuary.hasTerminated() )
            this.actuary.terminate();

        GCVSocket.closeSocketConnection(
                    this.channel.getOtherStationProperties().ip().toString()
                            + this.channel.getOtherStationProperties().port());

        this.channel.close();
    }

    public void send( byte[] data) throws IOException, InterruptedException {
        if(this.actuary.hasTerminated())
            throw new IOException("GCVSocket has disconnected");

        this.actuary.send(data);
    }

    public void send( InputStream io ) throws  IOException{
        if(this.actuary.hasTerminated())
            throw new IOException("GCVSocket has disconnected");

        this.actuary.send(io);
    }

    public void sendWhenReady( InputStream io ) throws  IOException, InterruptedException{
        if(this.actuary.hasTerminated())
            throw new IOException("GCVSocket has disconnected");

        this.actuary.sendWhenReady(io);
    }

    public OutputStream send() throws  IOException{
        if(this.actuary.hasTerminated())
            throw new IOException("GCVSocket has disconnected");

        PipedOutputStream producer = new PipedOutputStream();
        PipedInputStream consumer = new PipedInputStream(producer);
        this.actuary.send(consumer);
        return producer;
    }

    public InputStream receive() throws InterruptedException {
        return this.actuary.getStream();
    }

    public InputStream receiveWhenReady() throws InterruptedException {
        return this.actuary.getStreamWhenReady();
    }

    void restart() throws IOException {
        this.channel.sendPacket( new HI(
                (short)0,
                this.actuary.connectionTime(),
                this.channel.getSelfStationProperties().mtu(),
                maxWindow
        ));
    }

}
