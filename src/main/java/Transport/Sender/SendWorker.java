package Transport.Sender;

import Transport.FlowWindow;
import Transport.Start.SenderProperties;
import Transport.TransportChannel;

import java.io.IOException;
import java.io.NotActiveException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class SendWorker extends TimerTask {

    private Accountant send_buffer;
    private TransportChannel channel;
    private Timer send_time;
    private AtomicBoolean active = new AtomicBoolean(true);
    private FlowWindow flow_window;

    public SendWorker(TransportChannel ch, Accountant send, long period, SenderProperties properties, FlowWindow window) throws NotActiveException {
        this.send_buffer = send;
        this.channel = ch;
        this.send_time = new Timer();
        long period1 = period;
        this.send_time.scheduleAtFixedRate( this, 0, this.period1);
        this.flow_window = window;
        SenderProperties properties1 = properties;
    }

    public void stop(){
        this.active.set(false);
        send_time.cancel();
    }

    public void run(){
        try {

            if( active.get() ) {

                for(int i= 0; i< this.flow_window.value() ; i++)
                    channel.sendPacket(send_buffer.get());
            }

        }catch ( InterruptedException| IOException e){
            e.printStackTrace();
        }
    }
}
