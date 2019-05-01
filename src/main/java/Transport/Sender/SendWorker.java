package Transport.Sender;

import Test.Debugger;
import Transport.Executor;
import Transport.GCVConnection;
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
            Debugger.log("rate " + this.properties.window().uploadSpeed() + " Mb/s" );
            if( active.get() ) {
                int it =this.properties.window().congestionWindowValue();
                for(int i = 0; i< it ; i++){
                    DataPacket packet = sendBuffer.poll();
                    if( packet != null) {
                        channel.sendPacket(packet);
                    }
                }

                if( this.properties.isPersistent() && this.properties.window().rttHasPassed() )
                    Executor.add(Executor.ActionType.KEEPALIVE);

            }
        }catch (NotActiveException other){
            active.set(false);
        } catch ( InterruptedException| IOException e){
            e.printStackTrace();
        }
    }
}