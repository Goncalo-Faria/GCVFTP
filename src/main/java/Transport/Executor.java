package Transport;

import java.lang.Runnable;
import java.util.concurrent.LinkedBlockingQueue;

import Transport.Receiver.ReceiveGate;
import java.util.concurrent.atomic.AtomicBoolean;
import Transport.Sender.SendGate;

public class Executor implements Runnable{
    
    enum ActionType{
        CONTROL,
        DATA
    }

    private static LinkedBlockingQueue<ActionType> executorQueue = new LinkedBlockingQueue<ActionType>();
    
    public static void add(ActionType action) throws InterruptedException{
        executorQueue.put(action);
    }

    private static void get(Executor self) throws InterruptedException{
        switch( executorQueue.take() ){
            case CONTROL : self.control(); break;
            case DATA :  self.data(); break;
        }
    }

    private SendGate sgate;
    private ReceiveGate rgate;
    private AtomicBoolean active = new AtomicBoolean(true);

    Executor(SendGate sgate, ReceiveGate rgate){
        this.sgate = sgate;
        this.rgate = rgate;
    }

    public void terminate(){
        this.active.set(false);
    }

    private void control(){

    }

    private void data(){

    }

    public void run(){
        try{
            while( active.get() )
                Executor.get(this);
        }catch( InterruptedException e ){
            e.printStackTrace();
        }
    }
}