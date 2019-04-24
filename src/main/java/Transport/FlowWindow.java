package Transport;

import Transport.ControlPacketTypes.OK;
import Transport.ControlPacketTypes.SURE;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowWindow {

    private final LocalDateTime connectionStartTime = LocalDateTime.now();

    private AtomicInteger rtt = new AtomicInteger(1000);
    private AtomicInteger rttVar =  new AtomicInteger(3600 * 1000);


    private AtomicInteger timeLastReceived = new AtomicInteger(0);
    private AtomicInteger timeLastSent = new AtomicInteger(-1);
    private AtomicInteger timeLastNackSent = new AtomicInteger(0);

    private AtomicInteger lastSureReceived = new AtomicInteger(-1);
    private AtomicInteger lastOkSent = new AtomicInteger(0);
    private AtomicInteger lastOkReceived = new AtomicInteger(0);


    private AtomicInteger receiverBuffer = new AtomicInteger(GCVConnection.receive_buffer_size);

    private ConcurrentHashMap<Integer,Integer> sentOkCache = new ConcurrentHashMap<>();

    private AtomicInteger congestionWindowSize = new AtomicInteger(GCVConnection.initial_window_size);

    private final int maxWindowSize;

    private volatile long packetArrivalRate;
    private volatile long estimatedLinkCapacity;
    private volatile double sendPeriod;

    public FlowWindow( int maxWindow ){
        this.maxWindowSize = maxWindow;
    }

    public int congestionWindowValue(){
        return congestionWindowSize.get();
    }

    public int getMaxWindowSize(){
        return maxWindowSize;
    }

    private int getTimeout(){
        /* calculate the current timeout time */
        return rtt.get() + 4 * rttVar.get();
    }

    void sentOk(OK packet){
        sentOkCache.put( packet.getSeq(), packet.getTimestamp() );
        setLastSentOk( packet.getSeq() );
    }

    public boolean sentNope(){
        int curtime = connectionTime();

        if( curtime - timeLastNackSent.get() > getTimeout() ){
            timeLastNackSent.set(curtime);
            return true;
        }else{
            return false;
        }
    }

    void boot(int lastOkSent, int lastOkReceived){
        this.lastOkSent.set(lastOkSent);
        this.lastOkReceived.set(lastOkReceived);
        this.lastSureReceived.set(lastOkSent);
    }

    public int rtt(){
        return rtt.get();
    }

    void setRtt( int rtt){
        this.rtt.set(rtt);
    }

    void setRttVar( int rttVar){
        this.rttVar.set(rttVar);
    }

    void setReceiverBuffer(int window){
        this.receiverBuffer.set(window);
    }

    public int rttVar(){
        return rttVar.get();
    }

    void sentSure(SURE packet ){

        int seq = packet.getOK();
        int curtime = connectionTime();

        if( seq != -1){
            Integer timestamp = sentOkCache.remove(seq);

            if (timestamp != null){
                int srtt = curtime - timestamp;

                rtt.set((int)((1 - GCVConnection.rrt_factor) * rtt.get() + GCVConnection.rrt_factor * srtt));

                rttVar.set((int)((1 - GCVConnection.var_rrt_factor) * rttVar.get()
                        + GCVConnection.var_rrt_factor * Math.abs(srtt - rtt.get())));
            }

            lastSureReceived.getAndUpdate(x -> (x > seq) ? x : seq );

        }else{
            rttVar.set(rtt.get());
        }

        System.out.println("rtt: " + this.rtt.get() + " var: " + this.rttVar.get() +  " ");
    }

    public int connectionTime(){
        return (int)this.connectionStartTime.until(LocalDateTime.now(), ChronoUnit.MILLIS);
    }

    void setLastSentOk(int lastSentAck ){
        this.lastOkSent.getAndUpdate(x -> (x > lastSentAck) ? x : lastSentAck );
    }

    int getLastSentOk(){
        return this.lastOkSent.get();
    }

    public void setLastReceivedOk(int curReceivedOk ){
        this.lastOkReceived.getAndUpdate(x -> (x > curReceivedOk) ? x : curReceivedOk );
    }

    int getLastReceivedOk( ){ return this.lastOkReceived.get(); }

    void syn(){

    }

    boolean hasTimeout(){
        int difs = this.connectionTime() - this.timeLastReceived.get();
        return(difs > this.getTimeout());
    }

    void gotTransmission(){
        this.timeLastReceived.set(this.connectionTime());
    }

    public void sentTransmission(){ this.timeLastSent.set(this.connectionTime()); }

    public boolean synHasPassed(){
        return (this.connectionTime() - this.timeLastSent.get() > 100);
    }

    boolean okMightHaveBeenLost(){
        if( this.lastSureReceived.get() < this.lastOkSent.get() )
            return (this.connectionTime() - this.sentOkCache.get( this.lastOkSent.get())) > this.rtt.get();


        return false;
    }
}
