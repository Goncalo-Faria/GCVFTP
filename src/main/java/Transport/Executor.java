package Transport;

import java.io.*;
import java.lang.Runnable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import Transport.Receiver.ReceiveGate;
import java.util.concurrent.atomic.AtomicBoolean;

import Transport.Sender.SendGate;
import Transport.Unit.*;
import Transport.ControlPacketTypes.*;

public class Executor implements Runnable{
    /*
    
    */ 
    public enum ActionType{
        CONTROL,
        DATA,
        SYN,
        KEEPALIVE
    }

    private static LinkedBlockingDeque<ActionType> executorQueue = new LinkedBlockingDeque<ActionType>();
    
    public static void add(ActionType action) throws InterruptedException{
        switch( action ){
            case CONTROL : executorQueue.putFirst(action); break;
            case SYN : executorQueue.putFirst(action);break;
            case DATA : executorQueue.put(action); break;
            case KEEPALIVE: executorQueue.put(action); break;
        }
    }

    private static void get(Executor self) throws InterruptedException{

        switch( executorQueue.take() ){
            case CONTROL : self.control(); break;
            case DATA :  self.data(); break;
            case SYN :  self.syn(); break;
            case KEEPALIVE: self.keepalive(); break;
        }
    }

    private SendGate sgate; /* mandar pacotes de controlo */
    private ReceiveGate rgate; /* tirar pacotes */
    private ConcurrentHashMap< Integer, ExecutorPipe > map = new ConcurrentHashMap<>();
    private LinkedBlockingQueue<ExecutorPipe> socketOutput = new LinkedBlockingQueue<>();
    private AtomicBoolean active = new AtomicBoolean(true);
    private FlowWindow window;

    Executor(SendGate sgate, ReceiveGate rgate, FlowWindow window){
        this.sgate = sgate;
        this.rgate = rgate;
        this.window = window;
        this.window.gotTransmission();
    }

    void terminate(short code) throws IOException{
        if(this.active.get()) {
            System.out.println("CHANNEL CLOSED");
            this.sgate.sendBye(code);
            this.active.set(false);
            this.sgate.close();
            this.rgate.close();
            this.map.clear();
        }
    }

    boolean hasTerminated(){
        return !this.active.get();
    }

    private void control(){
        this.window.gotTransmission();

        try{
            ControlPacket packet = this.rgate.control();
            switch( packet.getType() ){
                case HI: hi((HI)packet); break;
                case OK: ok((OK)packet); break;
                case SURE: sure((SURE)packet); break;
                case BYE: bye((BYE)packet); break;
                case SUP: sup((SUP)packet); break;
                case FORGETIT: forgetit((FORGETIT)packet); break;
                case NOPE: nope((NOPE)packet); break;
            }
        }catch( InterruptedException e ){
            e.printStackTrace();
        }
    }

    public void send( InputStream io ) throws InterruptedException,IOException{
        this.sgate.send(io);
    }

    public void send( byte[] data ) throws InterruptedException,IOException{
        this.sgate.send(data);
    }

    public int connectionTime(){
        return this.window.connectionTime();
    }

    InputStream getStream() throws InterruptedException{
        return this.socketOutput.take().consumer;
    }


    private void data(){
        /* distribuir os dados em streams */
        /* encaminhar para streams */
        /*-------------------------*/
        this.window.gotTransmission();


        try{
            DataPacket packet = this.rgate.data();

            //System.out.println(" ::::> DATA <:::: " + packet.getSeq() +  " ops ::" );

            if ( packet.getFlag().equals(DataPacket.Flag.FIRST) || packet.getFlag().equals(DataPacket.Flag.SOLO) ){
                ExecutorPipe inc = new Executor.ExecutorPipe();
                this.map.put(packet.getMessageNumber(), inc );
            }

            this.map.
                get(packet.getMessageNumber()).
                    producer.
                        write(packet.getData(),
                                0, packet.getData().length);

            if ( packet.getFlag().equals(DataPacket.Flag.LAST) || packet.getFlag().equals(DataPacket.Flag.SOLO) ){

                ExecutorPipe ep = this.map.remove(packet.getMessageNumber());
                this.socketOutput.put(ep);
                ep.close();
            }

            
        }catch( IOException|InterruptedException e ){
            e.printStackTrace();
        }
    }

    private void syn() {
        /*verificar condições maradas e mandar shouldSendNope ou ack */
        this.window.syn();
        if( this.window.hasTimeout() ){
            //try {
                //this.sgate.sendForgetit((short) 0);/*especifica o stream a fechar 0 significa todos*/
                /* do something about it*/
                /* like close socket */
                //this.terminate((short)303);
                System.out.println("TIMEOUT");

            //}catch(IOException e){
                //e.printStackTrace();
            //}

        }else {
            try {
                int curack = this.rgate.getLastSeq();

                if (curack > this.window.getLastSentOk() ){
                    /**/
                    this.window.sentOk( this.sgate.sendOk((short)0,curack,this.rgate.getWindowSize()) );
                    System.out.println(curack +" SENT ACK " + " :: " + curack + " win" + this.rgate.getWindowSize()  );

                } else {

                    if( this.window.shouldSendNope() )
                        this.sgate.sendNope(this.rgate.getLossList());

                    if( this.window.okMightHaveBeenLost() ){
                        this.window.sentOk( this.sgate.sendOk((short)0,curack,this.rgate.getWindowSize()) );
                        System.out.println(curack +" ReSENT ACK " + " :: " + this.window.getLastSentOk()  );

                    }
                }

                int lastReceivedOk = this.window.getLastReceivedOk();

                if( lastReceivedOk > this.window.getLastSentSure()){
                    this.window.setLastSentSure(lastReceivedOk);
                    this.sgate.sendSure(lastReceivedOk);
                    System.out.println("SENT SURE");
                }else{
                    this.window.deactivateCongestionControl();
                }

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void keepalive(){
        try {
            this.sgate.sendSup((short) 0);
            System.out.println("SENT KEEPALIVE");
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void hi(HI packet){
        System.out.println(" ::::> received an hi packet <:::: ");
    }

    private void ok(OK packet) throws InterruptedException{
        System.out.println(" ::::> received an " + packet.getSeq() + " ok " + packet.getSeq() + " packet <::::");
        try{
            this.window.setRtt(packet.getRtt());
            this.window.setRttVar(packet.getRttVar());
            this.window.setReceiverBuffer(packet.getWindow());
            this.window.receivedOk(packet.getSeq());
            this.sgate.release(packet.getSeq());
        }catch(NotActiveException e){
            e.printStackTrace();
        }
    }

    private void sure(SURE packet){
        System.out.println(" ::::> received an " + packet.getOK() + " receivedSure " + packet.getOK() + " packet <::::");
        this.window.receivedSure(packet.getOK());
    }
    private void bye(BYE packet){
        System.out.println(" ::::> received a bye packet <::::");
        try{
            this.terminate((short)0);
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void sup(SUP packet){
        System.out.println(" ::::> received a sup packet <::::");

    }
    private void forgetit(FORGETIT packet){
        System.out.println(" ::::> received a forgetit packet <::::");
        short extcode = packet.getExtendedtype();

        try {
            if (extcode == 0)
                this.terminate((short)0);
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private void nope(NOPE packet) {
        System.out.println(" ::::> received a shouldSendNope packet <::::");
        try{
            List<Integer> lost = packet.getLossList();
            this.window.activateCongestionControl();
            //this.window.receivedOk(lost.get(0)-1);
            //this.sgate.release(lost.get(0)-1);
            //this.sgate.release(lost.get(0)-1);
            this.sgate.retransmit(lost);
        }catch (InterruptedException|NotActiveException e){
            e.printStackTrace();
        }

    }

    public void run(){
        try{
            while( active.get() ) {
                Executor.get(this);
            }
        }catch( InterruptedException e ){
            e.printStackTrace();
        }
    }

    class ExecutorPipe{

        PipedInputStream consumer;
        PipedOutputStream producer;

        ExecutorPipe() throws IOException {
            this.producer = new PipedOutputStream();
            this.consumer = new PipedInputStream(this.producer);
        }

        void close() throws IOException {
            this.producer.flush();
            this.producer.close();
        }
    }
}