package Transport;

import Transport.Start.SenderProperties;
import Transport.Unit.Packet;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Sender extends TimerTask {

    private Accountant<Packet> send_buffer;
    private TransportChannel channel;
    private Timer send_time;
    private long period;
    private AtomicBoolean active = new AtomicBoolean(true);
    private int flow_window;
    private SenderProperties properties;

    public Sender(TransportChannel ch, Accountant<Packet> send, long period, SenderProperties properties){
        this.send_buffer = send;
        this.channel = ch;
        this.send_time = new Timer();
        this.period = period;
        this.send_time.scheduleAtFixedRate( this, 0, this.period);
        this.flow_window = send_buffer.window();
        this.properties = properties;
    }

    public void stop(){
        this.active.set(false);
    }

    public void run(){
        try {

            if( active.get() ) {
                this.flow_window = send_buffer.window();

                for(int i= 0; i< this.flow_window ; i++)
                    channel.sendPacket(send_buffer.get());
            }

        }catch ( InterruptedException| IOException e){
            e.printStackTrace();
        }
    }
}
