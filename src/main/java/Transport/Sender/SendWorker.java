package Transport.Sender;

import Transport.Executor;
import Transport.TransportChannel;
import Transport.Unit.DataPacket;

import java.io.IOException;
import java.io.NotActiveException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;


public class SendWorker extends TimerTask {

    private Accountant sendBuffer;
    private TransportChannel channel;
    private Timer sendTimer;
    private AtomicBoolean active = new AtomicBoolean(true);
    private SenderProperties properties;

    public SendWorker(TransportChannel ch, Accountant send, long period, SenderProperties properties) throws NotActiveException {
        this.sendBuffer = send;
        this.channel = ch;
        this.sendTimer = new Timer();
        this.sendTimer.scheduleAtFixedRate( this, 0, period);
        this.properties = properties;
    }

    public void stop(){
        this.active.set(false);
        sendTimer.cancel();
    }

    public void run(){
        try {
            Executor.add(Executor.ActionType.SYN);
            if( active.get() ) {
                for(int i = 0; i< this.properties.window().congestionWindowValue() ; i++){
                    DataPacket packet = sendBuffer.poll();
                    if( packet != null) {
                        channel.sendPacket(packet);
                        System.out.println("SENT DATA");
                        this.properties.window().sentTransmission();
                    }
                }

                if( this.properties.isPersistent() && this.properties.window().synHasPassed() )
                    Executor.add(Executor.ActionType.KEEPALIVE);

            }
        }catch (NotActiveException other){
            active.set(false);
        } catch ( InterruptedException| IOException e){
            e.printStackTrace();
        }
    }
}
