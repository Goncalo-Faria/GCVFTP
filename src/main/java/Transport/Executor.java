package Transport;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.Runnable;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import Transport.Receiver.Examiner;
import Transport.Receiver.ReceiveGate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
            case SYN :

                //try {
                    //if (!executorQueue.peek().equals(ActionType.SYN)) {
                        executorQueue.putFirst(action);
                        //System.out. println(" PUT SYN ");
                    //}
                //}catch (NullPointerException e){
                 //   System.out. println(" PUT SYN ");
                  //  executorQueue.putFirst(action);
                //}
                break;
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
    private LinkedBlockingQueue<ExecutorPipe> socketoutput = new LinkedBlockingQueue<>();
    private AtomicBoolean active = new AtomicBoolean(true);
    private FlowWindow window;

    Executor(SendGate sgate, ReceiveGate rgate, FlowWindow window){
        this.sgate = sgate;
        this.rgate = rgate;
        this.window = window;
        this.window.gotTransmission();
    }

    public void terminate(short code) throws IOException{
        if(this.active.get()) {
            System.out.println("CHANNEL CLOSED");
            this.sgate.sendBye(code);
            this.active.set(false);
            this.sgate.close();
            this.rgate.close();
            this.map.clear();
        }
    }

    public boolean hasTerminated(){
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

    public InputStream getStream() throws InterruptedException{
        return this.socketoutput.take().consumer;
    }


    private void data(){
        /* distribuir os dados em streams */
        /* encaminhar para streams */
        /*-------------------------*/
        this.window.gotTransmission();


        try{
            DataPacket packet = this.rgate.data();

            System.out.println(" ::::> DATA <:::: " + packet.getSeq() +  " ops ::" );

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
                this.socketoutput.put(ep);
                ep.close();
            }

            
        }catch( IOException|InterruptedException e ){
            e.printStackTrace();
        }
    }

    private void syn() {
        /*verificar condições maradas e mandar nack ou ack */
        this.window.syn();

        if( this.window.hasTimeout() ){
            try {
                this.sgate.sendForgetit((short) 0);/*especifica o stream a fechar 0 significa todos*/
                /* do something about it*/
                /* like close socket */
                this.terminate((short)303);
                System.out.println("TIMEOUT");

            }catch(IOException e){
                e.printStackTrace();
            }

        }else {
            try {
                int curack = this.rgate.getLastSeq();

                if (curack > this.window.getLastSentAck() ) {
                    /**/
                    this.window.ack( this.sgate.sendOk((short)0,curack,this.rgate.getWindowSize()) );
                    System.out.println("SENT ACK");

                } else {

                    if( this.window.nack() ) {
                        this.sgate.sendNack(this.rgate.getLossList());
                        System.out.println("SENT NACK FAKE");
                    }

                }

                int curok = this.sgate.getLastOk();

                if(curok > this.window.getLastReceivedAck()){

                    this.sgate.sendSure(curok);
                    this.window.setLastReceivedAck(curok);
                    System.out.println("SENT SURE");
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
        System.out.println(" ::::> received an " + packet.getAck() + " ok " + packet.getAck() + " packet <::::");
        try{
            this.window.setRtt(packet.getRtt());
            this.window.setRttVar(packet.getRttVar());
            this.window.setReceiveBufferSize(packet.getWindow());
            this.sgate.gotok(packet.getAck());
        }catch(NotActiveException e){
            e.printStackTrace();
        }
    }
    private void sure(SURE packet){
        System.out.println(" ::::> received an " + packet.getOK() + " sure " + packet.getOK() + " packet <::::");
        //this.sgate.gotsure(packet.getOK());
        this.window.sure(packet);
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
        System.out.println(" ::::> received a nope packet <::::");
        try{
            this.sgate.gotnack(packet.getLossList());
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