package Transport;

import Test.Debugger;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;


import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

class ConnectionScheduler implements Runnable{


    private final DatagramSocket connection;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final BlockingQueue<StampedControlPacket> queue = new LinkedBlockingQueue<>();
    private final Timer alarm = new Timer(true);
    private LocalDateTime clearTime = LocalDateTime.now();
    private final ControlPacket.Type packetType;
    private int maxPacket;
    private final ConcurrentHashMap<String, GCVSocket> connections = new ConcurrentHashMap<>();

    ConnectionScheduler(int port,
                        long connection_request_ttl,
                        ControlPacket.Type control_packet_type,
                        int maxPacket)
            throws SocketException {

        this.packetType = control_packet_type;
        this.connection = new DatagramSocket(port);
        this.maxPacket = maxPacket;

       new Thread(this).start();

       Debugger.log( this.connection.getLocalPort() + "::" + this.connection.getLocalAddress());
       this.alarm.scheduleAtFixedRate(
                new RemoveExpired(),
                0 ,
                connection_request_ttl);
    }

    public ControlPacket get()
            throws InterruptedException{

        return queue.take().get();
    }

    ConnectionScheduler.StampedControlPacket getStamped()
            throws InterruptedException{

        return queue.take();
    }

    void announceConnection( String key , GCVSocket cs ){
        this.connections.put(key,cs);
    }

    void closeConnection( String key ){
        this.connections.remove(key);
    }

    public void run() {

        try {
            while (this.active.get()) {
                DatagramPacket packet = new DatagramPacket(new byte[this.maxPacket], this.maxPacket);
                try {
                    this.connection.receive(packet);

                    Packet synpacket = Packet.parse(packet.getData());

                    if (synpacket instanceof ControlPacket) {
                        ControlPacket cpacket = (ControlPacket) synpacket;
                        ControlPacket.Type packettype = cpacket.getType();

                        if (packettype.equals(this.packetType)) {
                            Debugger.log("got " + packet.getLength() + " bytes ::-:: ip = " + packet.getAddress() + " port= " + packet.getPort());

                            if (connections.containsKey(packet.getAddress().toString() + packet.getPort()))
                                connections.get(packet.getAddress().toString() + packet.getPort()).restart();

                            this.queue.put(new StampedControlPacket(cpacket, packet.getPort(), packet.getAddress()));
                        }
                    }
                }catch (StreamCorruptedException e){
                    ;// erro no pacote ignora.
                }
            }
        }catch( InterruptedException | IOException e){
            e.getStackTrace();
        }

    }

    void close(){
        this.active.set(false);
        this.alarm.cancel();
        this.connection.close();
    }

    private class RemoveExpired extends TimerTask{
        public void run(){
            queue.removeIf( p -> p.isRecent(clearTime) );
            clearTime = LocalDateTime.now();
        }
    }

    public class StampedControlPacket {
        private final ControlPacket obj;
        private final int port;
        private final InetAddress address;
        private final LocalDateTime t = LocalDateTime.now();

        StampedControlPacket(ControlPacket obj,int port, InetAddress address){
            this.obj = obj;
            this.port = port;
            this.address = address;
        }

        boolean isRecent( LocalDateTime cleartime){
            return cleartime.isAfter(t);
        }

        public ControlPacket get(){
            return this.obj;
        }

        int port(){ return this.port;}

        InetAddress ip(){ return this.address; }

    }
}
