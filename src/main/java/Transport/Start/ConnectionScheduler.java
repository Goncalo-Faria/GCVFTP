package Transport.Start;

import AgenteUDP.StreamIN;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConnectionScheduler implements Runnable{


    private final StreamIN connection;
    private final AtomicBoolean active = new AtomicBoolean(true);
    private BlockingQueue<StampedDatagramPacket> queue = new LinkedBlockingQueue<>();
    private final Timer alarm = new Timer();
    private LocalDateTime clearTime = LocalDateTime.now();

    ConnectionScheduler(int connection_request_capacity,
                        int maxcontrol,
                        InetAddress ip,
                        int port,
                        long connection_request_ttl)
            throws SocketException {

       this.connection = new StreamIN(
               connection_request_capacity,
               maxcontrol,ip,
               port);

       new Thread(this).start();

       this.alarm.scheduleAtFixedRate(
                new RemoveExpired(),
                0 ,
                connection_request_ttl);
    }

    public DatagramPacket get()
            throws InterruptedException{

        return queue.take().get();
    }

    public void run() {

        try {
            while (this.active.get() && connection.isActive()) {
                this.queue.put(new StampedDatagramPacket(this.connection.getDatagram()));

            }
        }catch( InterruptedException e){
            e.getStackTrace();
        }

    }

    public void close(){
        this.active.set(false);
        this.alarm.cancel();
        this.connection.stop();
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

    private class StampedDatagramPacket {
        private DatagramPacket obj;
        private LocalDateTime t = LocalDateTime.now();

        StampedDatagramPacket(DatagramPacket obj){
            this.obj = obj;
        }

        boolean isRecent( LocalDateTime cleartime){
            return cleartime.isAfter(t);
        }

        DatagramPacket get(){
            return this.obj;
        }

    }
}
