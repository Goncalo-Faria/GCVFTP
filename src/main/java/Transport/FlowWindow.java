package Transport;

import Transport.ControlPacketTypes.OK;
import Transport.ControlPacketTypes.SURE;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowWindow {

    private AtomicInteger rtt = new AtomicInteger(1000);
    private AtomicInteger rttVar =  new AtomicInteger(3600 * 1000);
    private AtomicInteger lastNack = new AtomicInteger(0);
    private AtomicInteger lastTimePacket = new AtomicInteger(0);

    private AtomicInteger lastSent = new AtomicInteger(0);


    private LocalDateTime connection_start_time = LocalDateTime.now();
    private AtomicInteger receiveBufferSize = new AtomicInteger(GCVConnection.receive_buffer_size);
    private volatile long packetArrivalRate;
    private volatile long estimatedLinkCapacity;
    private volatile double sendPeriod;
    private volatile long congestionWindowSize;

    private AtomicInteger lastsentack = new AtomicInteger(0);
    private AtomicInteger lastreceivedack = new AtomicInteger(0);

    private final int maxwindow;

    private ConcurrentHashMap<Integer,Integer> ack_cache = new ConcurrentHashMap<>();

    private AtomicInteger value = new AtomicInteger(GCVConnection.initial_window_size);

    public FlowWindow( int maxwindow){
        this.maxwindow = maxwindow;
    }

    public int value(){
        return value.get();
    }

    public int getMaxWindow(){
        return maxwindow;
    }

    public int getTimeout(){
        /* calculate the current timeout time */
        return rtt.get() + 4 * rttVar.get();
    }

    public void ack(OK packet){
        ack_cache.put( packet.getAck(), packet.getTimestamp() );

    }

    public boolean nack(){
        int curtime = connectionTime();

        if( curtime - lastNack.get() > getTimeout() ){
            lastNack.set(curtime);
            return true;
        }else{
            return false;
        }
    }

    public int rtt(){
        return rtt.get();
    }

    public void setRtt( int rtt){
        this.rtt.set(rtt);
    }

    public void setRttVar( int rttVar){
        this.rttVar.set(rttVar);
    }

    public void setReceiveBufferSize(int window){
        this.receiveBufferSize.set(window);
    }

    public int rttVar(){
        return rttVar.get();
    }

    public void sure( SURE packet ){

        int seq = packet.getOK();
        int curtime = connectionTime();

        if( seq != -1){
            Integer timestamp = ack_cache.remove(seq);

            if (timestamp != null){
                int srtt = curtime - timestamp;

                rtt.set((int)((1 - GCVConnection.rrt_factor) * rtt.get() + GCVConnection.rrt_factor * srtt));

                rttVar.set((int)((1 - GCVConnection.var_rrt_factor) * rttVar.get()
                        + GCVConnection.var_rrt_factor * Math.abs(srtt - rtt.get())));
            }
        }else{
            rttVar.set(rtt.get());
        }

        System.out.println("rtt: " + this.rtt.get() + " var: " + this.rttVar.get() +  " ");
    }

    public int connectionTime(){
        return (int)this.connection_start_time.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    }

    void setLastSentAck( int lastSentAck ){
        this.lastsentack.set(lastSentAck);
    }

    public int getLastSentAck(){
        return this.lastsentack.get();
    }

    void setLastReceivedAck( int lastReceivedAck ){
        this.lastreceivedack.set(lastReceivedAck);
    }

    public int getLastReceivedAck( ){
        return this.lastreceivedack.get();
    }

    void syn(){

    }

    public boolean hasTimeout(){
        int difs = this.connectionTime() - this.lastTimePacket.get();
        return(difs > this.getTimeout());
    }

    void gotTransmission(){
        this.lastTimePacket.set(this.connectionTime());
    }

    public void sentTransmission(){ this.lastSent.set(this.connectionTime()); }

    public boolean synHasPassed(){
        return (this.connectionTime() - this.lastSent.get() > 100);
    }

}
