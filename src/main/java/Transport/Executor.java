package Transport;

import java.beans.IntrospectionException;
import java.io.*;
import java.lang.Runnable;
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
    private AtomicInteger lastsentack;
    private AtomicInteger lastreceivedack;

    private AtomicInteger lastPacketTime = new AtomicInteger(-1);

    Executor(SendGate sgate, ReceiveGate rgate, int their_startseq, int our_start_seq){
        this.sgate = sgate;
        this.rgate = rgate;
        this.lastsentack = new AtomicInteger(their_startseq);
        this.lastreceivedack = new AtomicInteger(our_start_seq);
    }

    public void terminate(){
        this.active.set(false);
    }

    private void control(){
        this.lastPacketTime.set(this.sgate.connection_time());
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
        this.lastPacketTime.set(this.sgate.connection_time());
        try{
            DataPacket packet = this.rgate.data();

            System.out.println(" ::::> DATA <:::: " + packet.getSeq() +  " ops ::" );

            if ( packet.getFlag().equals(DataPacket.Flag.FIRST) || packet.getFlag().equals(DataPacket.Flag.SOLO) ){
                ExecutorPipe inc = new Executor.ExecutorPipe();
                this.map.put(packet.getMessageNumber(), inc );
                this.socketoutput.put(inc);

            }
                
            this.map.
                get(packet.getMessageNumber()).
                    producer.
                        write(packet.getData(),
                                0, packet.getData().length);

            if ( packet.getFlag().equals(DataPacket.Flag.LAST) || packet.getFlag().equals(DataPacket.Flag.SOLO) ){
                this.map.get(packet.getMessageNumber()).close();
                this.map.remove(packet.getMessageNumber());
            }

            
        }catch( IOException|InterruptedException e ){
            e.printStackTrace();
        }
    }

    private void syn(){
        /*verificar condições maradas e mandar nack ou ack */
        int difs = this.sgate.connection_time() - this.lastPacketTime.get();

        if( difs >= this.sgate.getTimeout() ){
            try {
                this.sgate.sendForgetit((short) 303);
                /* do something about it*/
                this.sgate.sendBye((short)303);
                /* like close socket */
                System.out.println("TIMEOUT");
            }catch(IOException e){
                e.printStackTrace();
            }

        }else {
            try {
                int curack = this.rgate.getLastSeq();
                if (curack > this.lastsentack.get()) {
                    /**/
                    this.sgate.sendOk((short)0,curack);
                    this.lastsentack.set(curack);
                    System.out.println("SENT ACK");
                } else {
                    this.sgate.sendNack((short)0,this.rgate.getLossList());
                }
                int curok = this.sgate.getLastOk();

                if(curok > this.lastreceivedack.get()){
                    this.sgate.sendSure(curok);
                    this.lastreceivedack.set(curack);
                    System.out.println("SENT SURE" + " curok : " + curok + " receivedack : " + this.lastreceivedack.get());
                }

            } catch (IOException e) {
                System.out.println("SENT NACK");
                e.printStackTrace();
            }
        }
    }

    private void keepalive(){
        try {
            this.sgate.sendSup((short) 0);
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
            this.sgate.gotok(packet.getAck());
        }catch(NotActiveException e){
            e.printStackTrace();
        }
    }
    private void sure(SURE packet){
        System.out.println(" ::::> received an " + packet.getOK() + " sure " + packet.getOK() + " packet <::::");
    }
    private void bye(BYE packet){
        System.out.println(" ::::> received a bye packet <::::");
    }
    private void sup(SUP packet){
        System.out.println(" ::::> received a sup packet <::::");
        this.lastPacketTime.set(this.sgate.connection_time());
    }
    private void forgetit(FORGETIT packet){
        System.out.println(" ::::> received a forgetit packet <::::");
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