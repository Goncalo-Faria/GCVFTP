package Transport.Receiver;

import Transport.TransportChannel;
import Transport.Unit.Packet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveWorker implements Runnable{
    /*tem de verificar se há espaço no buffer, caso contrário dá drop*/

    /* para impedir denial of service atacks */
    private AtomicBoolean active = new AtomicBoolean(true);
    private TransportChannel ch;
    private ReceiverProperties properties;
    private Examiner buffer;

    public ReceiveWorker(ReceiverProperties properties, TransportChannel ch, Examiner buffer){
        this.ch = ch;
        this.properties = properties;
        this.buffer = buffer;
    }

    public void stop(){
        this.active.set(false);

    }

    public void run(){
        try {

            while( active.get() ) {
                Packet packet = ch.receivePacket();
                this.buffer.supply(packet);
            }

        }catch ( InterruptedException | IOException e){
            e.printStackTrace();
        }
    }
}
