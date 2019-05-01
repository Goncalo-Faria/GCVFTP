package Transport.Receiver;

import Transport.TransportChannel;
import Transport.Unit.Packet;

import java.io.NotActiveException;
import java.lang.Thread;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveWorker implements Runnable{
    /*tem de verificar se há espaço no buffer, caso contrário dá drop*/

    /* para impedir denial of service atacks */
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final TransportChannel channel;
    private final ReceiverProperties properties;
    private final Examiner buffer;

    public ReceiveWorker(TransportChannel channel, Examiner buffer, ReceiverProperties properties, int numThreads ){
        this.channel = channel;
        this.properties = properties;
        this.buffer = buffer;

        for( int i = 0; i< numThreads ; i++)
        (new Thread(this)).start();
    }

    public void stop(){
        this.active.set(false);
    }

    public void run(){
        try {

            while( active.get() ) {
                Packet packet = channel.receivePacket();
                this.buffer.supply(packet);
            }

        }catch (NotActiveException other){
            active.set(false);
        }catch ( InterruptedException| IOException e) {
            e.printStackTrace();
        }
    }
}
