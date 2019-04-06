package Transport.Start;

import Transport.GCVConnection;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionScheduler implements Runnable{


    private final DatagramSocket connection;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private BlockingQueue<StampedControlPacket> queue = new LinkedBlockingQueue<>();
    private final Timer alarm = new Timer();
    private LocalDateTime clearTime = LocalDateTime.now();
    private ControlPacket.Type packet_type;
    private int maxpacket = 0;

    ConnectionScheduler(int port,
                        long connection_request_ttl,
                        ControlPacket.Type control_packet_type,
                        int maxpacket)
            throws SocketException {

        this.packet_type = control_packet_type;
        this.connection = new DatagramSocket(port);
        this.maxpacket = maxpacket;

       new Thread(this).start();

       System.out.println( this.connection.getLocalPort() + "::" + this.connection.getLocalAddress());
       this.alarm.scheduleAtFixedRate(
                new RemoveExpired(),
                0 ,
                connection_request_ttl);
    }

    public ControlPacket get()
            throws InterruptedException{

        return queue.take().get();
    }

    ConnectionScheduler.StampedControlPacket getstamped()
            throws InterruptedException{

        return queue.take();
    }

    public void run() {

        try {
            while (this.active.get()) {
                DatagramPacket packet = new DatagramPacket(new byte[this.maxpacket], this.maxpacket);
                this.connection.receive(packet);

                System.out.println(packet.getLength());

                Packet synpacket = Packet.parse(packet.getData());

                if(synpacket instanceof ControlPacket){
                    ControlPacket cpacket = (ControlPacket)synpacket;
                    ControlPacket.Type packettype = cpacket.getType();

                    if(packettype.equals(this.packet_type))
                        this.queue.put(new StampedControlPacket(cpacket, packet.getPort(), packet.getAddress()));

                }
            }
        }catch( InterruptedException | IOException e){
            e.getStackTrace();
        }

    }

    public void close(){
        this.active.set(false);
        this.alarm.cancel();
        this.connection.close();
    }

    public boolean isActive(){
        return this.active.get();
    }

    public boolean isCloses(){
        return !this.active.get();
    }

    private class RemoveExpired extends TimerTask{
        public void run(){
            queue.removeIf( p -> p.isRecent(clearTime) );
            clearTime = LocalDateTime.now();
        }
    }

    class StampedControlPacket {
        private ControlPacket obj;
        private int port;
        private InetAddress address;
        private LocalDateTime t = LocalDateTime.now();

        StampedControlPacket(ControlPacket obj,int port, InetAddress address){
            this.obj = obj;
            this.port = port;
            this.address = address;
        }

        boolean isRecent( LocalDateTime cleartime){
            return cleartime.isAfter(t);
        }

        ControlPacket get(){
            return this.obj;
        }

        int port(){ return this.port;}

        InetAddress ip(){ return this.address; }

    }
}
