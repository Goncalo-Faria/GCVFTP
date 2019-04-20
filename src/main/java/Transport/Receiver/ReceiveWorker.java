package Transport.Receiver;

import Transport.TransportChannel;
import Transport.Unit.Packet;

import java.lang.Thread;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReceiveWorker implements Runnable{
    /*tem de verificar se há espaço no buffer, caso contrário dá drop*/

    /* para impedir denial of service atacks */
    private AtomicBoolean active = new AtomicBoolean(true);
    private TransportChannel ch;
    private ReceiverProperties properties;
    private Examiner buffer;

    public ReceiveWorker(TransportChannel ch, Examiner buffer, ReceiverProperties properties){
        this.ch = ch;
        this.properties = properties;
        this.buffer = buffer;
        (new Thread(this)).start();
    }

    public void stop(){
        this.active.set(false);

    }

    public void run(){
        try {

            while( active.get() ) {
                Packet packet = ch.receivePacket();
                this.buffer.supply(packet);
                System.out.println("got them");
            }

        }catch ( InterruptedException | IOException e){
            e.printStackTrace();
        }
    }
}
