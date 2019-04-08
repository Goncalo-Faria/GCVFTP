package Transport;

import Transport.Unit.Packet;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Sender extends TimerTask {

    private Accountant<Packet> send_buffer;
    private TransportChannel channel;
    private Timer send_time;
    private long period;
    private AtomicBoolean active = new AtomicBoolean(true);
    private AtomicInteger flow_window = new AtomicInteger(2);

    public Sender( TransportChannel ch, Accountant<Packet> send){
        this.send_buffer = send;
        this.channel = ch;
        this.send_time = new Timer();
        this.send_time.scheduleAtFixedRate( this, 0, period);
    }


    public void stop(){
        this.active.set(false);
    }

    public void window(int x){
        this.flow_window.set(x);
    }

    public void run(){
        try {

            if( active.get() ) {
                int flow = this.flow_window.get();
                for(int i= 0; i< flow ; i++)
                    channel.sendPacket(send_buffer.get());

            }

        }catch ( InterruptedException| IOException e){
            e.printStackTrace();
        }
    }
}
