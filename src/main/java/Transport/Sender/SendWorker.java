package Transport.Sender;

import Transport.ControlPacketTypes.SUP;
import Transport.Executor;
import Transport.TransportChannel;
import Transport.Unit.DataPacket;

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
    private SenderProperties properties;

    public SendWorker(TransportChannel ch, Accountant send, long period, SenderProperties properties) throws NotActiveException {
        this.send_buffer = send;
        this.channel = ch;
        this.send_time = new Timer();
        this.send_time.scheduleAtFixedRate( this, 0, period);
        this.properties = properties;
    }

    public void stop(){
        this.active.set(false);
        send_time.cancel();
    }

    public void run(){
        try {
            Executor.add(Executor.ActionType.SYN);
            if( active.get() ) {
                for(int i= 0; i< this.properties.window().value() ; i++){
                    DataPacket packet = send_buffer.poll();
                    if( packet != null) {
                        channel.sendPacket(packet);
                        System.out.println("SENT DATA");
                        this.properties.window().sentTransmission();
                    }
                }

                if( this.properties.isPersistent() && this.properties.window().synHasPassed() )
                    Executor.add(Executor.ActionType.KEEPALIVE);
                
            }
        }catch ( InterruptedException| IOException e){
            e.printStackTrace();
        }
    }
}
