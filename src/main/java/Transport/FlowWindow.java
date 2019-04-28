package Transport;

import Transport.ControlPacketTypes.OK;
import Transport.ControlPacketTypes.SURE;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.PrimitiveIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowWindow {

    private final LocalDateTime connectionStartTime = LocalDateTime.now();

    private AtomicInteger rtt = new AtomicInteger(1000);
    private AtomicInteger rttVar =  new AtomicInteger(3600 * 1000);


    private AtomicInteger timeLastReceived = new AtomicInteger(0);
    private AtomicInteger timeLastSent = new AtomicInteger(-1);
    private AtomicInteger timeLastNackSent = new AtomicInteger(0);
    private AtomicInteger timeLastOkReceived = new AtomicInteger(-1);

    private AtomicInteger lastSureReceived = new AtomicInteger(-1);
    private AtomicInteger lastSureSent = new AtomicInteger(-1);
    private AtomicInteger lastOkSent = new AtomicInteger(0);
    private AtomicInteger lastOkReceived = new AtomicInteger(this.connectionTime());

    private AtomicInteger receiverBuffer;

    private ConcurrentHashMap<Integer,Integer> sentOkCache = new ConcurrentHashMap<>();

    private AtomicInteger congestionWindowSize = new AtomicInteger(GCVConnection.initial_window_size);

    private AtomicBoolean congestionControl = new AtomicBoolean(false);

    private volatile long packetArrivalRate;
    private volatile long estimatedLinkCapacity;
    private volatile double sendPeriod;

    private final int maxWindow;

    public FlowWindow( int maxWindow ){
        receiverBuffer = new AtomicInteger(maxWindow);
        this.maxWindow = maxWindow;
    }

    public int congestionWindowValue(){
        return congestionWindowSize.get();
    }

    public int getMaxWindowSize(){
        return maxWindow;
    }

    void sentOk(OK packet){
        sentOkCache.put( packet.getSeq(), packet.getTimestamp() );
        setLastSentOk( packet.getSeq() );
    }

    boolean shouldSendNope(){
        int curTime = connectionTime();

        if( curTime - timeLastNackSent.get() > (rtt.get() + 4 * rttVar.get()) ){
            timeLastNackSent.set(curTime);
            return true;
        }else{
            return false;
        }
    }

    void activateCongestionControl(){
        this.congestionControl.set(true);
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

    void receivedSure(int seq){

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

        //System.out.println("rtt: " + this.rtt.get() + " var: " + this.rttVar.get() +  " ");
    }

    void deactivateCongestionControl(){
        this.congestionControl.set(false);
    }

    void receivedNotCoolOk(int seq) {

        this.timeLastOkReceived.set(this.connectionTime());
        int lastseq = this.lastOkReceived.get();

        this.lastOkReceived.updateAndGet(x -> (x > seq) ? x : seq );

    }

    void receivedOk(int seq) {

        this.timeLastOkReceived.set(this.connectionTime());
        int lastseq = this.lastOkReceived.get();

        if( (seq == this.lastOkReceived.updateAndGet(x -> (x > seq) ? x : seq )) ){

            if( !this.congestionControl.get() ) {
                System.out.println("#################################################[]");
                this.congestionWindowSize.getAndAdd(  (seq - lastseq) > 2 ? (seq - lastseq) : 2 );

            }else{
                this.congestionWindowSize.getAndAdd(  (seq - lastseq)*GCVConnection.additive_fraction > 2 ? (int)((seq - lastseq)*GCVConnection.additive_fraction)  : 2 );
            }

            int buffsize = this.receiverBuffer.get();
            int win = this.congestionWindowSize.getAndUpdate(x -> (x < buffsize) ? x : buffsize);
            /* trys to not pass the window*/
            if (win == maxWindow)
                this.activateCongestionControl();

        }
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

    int getLastReceivedOk( ){ return this.lastOkReceived.get(); }

    int getLastSentSure() { return this.lastSureSent.get(); }

    void setLastSentSure( int seq ) {
        this.lastSureSent.getAndUpdate(x -> (x > seq) ? x : seq );
    }

    void syn(){
        if( congestionControl.get() ) {
            if ((this.connectionTime() - this.timeLastOkReceived.get()) > this.rtt.get() + 4 * this.rttVar.get() ) {
                /* mul decrease */
                this.multiplicativeDecrease();
            }else{

            }

        }
    }

    public float uploadSpeed(){
        return GCVConnection.stdmtu * (float)this.congestionWindowValue() * 10/1000000;
    }

    private void multiplicativeDecrease(){
        this.congestionWindowSize.updateAndGet( x -> ( x > 1 ) ? (int)(GCVConnection.decrease_factor * x) : 1 );
    }

    boolean hasTimeout(){
        int difs = this.connectionTime() - this.timeLastReceived.get();
        return(difs > 4*(rtt.get() + 4 * rttVar.get()) );
    }

    public void gotTransmission(){
        this.timeLastReceived.set(this.connectionTime());
    }

    public void sentTransmission(){ this.timeLastSent.set(this.connectionTime()); }

    public boolean rttHasPassed(){
        return (this.connectionTime() - this.timeLastSent.get() > this.rtt.get());
    }

    boolean okMightHaveBeenLost(){
        if( this.lastSureReceived.get() < this.lastOkSent.get() )
            return (this.connectionTime() - this.sentOkCache.get( this.lastOkSent.get())) > this.rtt.get();


        return false;
    }
}
