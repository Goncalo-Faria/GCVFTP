package Transport;

import Transport.ControlPacketTypes.NOPE;
import Transport.ControlPacketTypes.OK;
import Transport.ControlPacketTypes.SURE;
import Transport.Unit.DataPacket;

import javax.xml.crypto.Data;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.PrimitiveIterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowWindow {

    private final LocalDateTime connectionStartTime = LocalDateTime.now();

    private AtomicInteger rtt = new AtomicInteger(100);
    private AtomicInteger rttVar =  new AtomicInteger(100);


    private AtomicInteger timeLastReceived = new AtomicInteger(0);
    private AtomicInteger timeLastSent = new AtomicInteger(0);

    private AtomicInteger timeLastNackSent = new AtomicInteger(0);
    private AtomicInteger timeLastSureSent = new AtomicInteger(0);

    private AtomicInteger timeLastNackReceived = new AtomicInteger(0);
    private AtomicInteger timeLastSureReceived = new AtomicInteger(0);
    private AtomicInteger timeLastOkReceived = new AtomicInteger(0);

    private AtomicInteger lastSureReceived = new AtomicInteger(0);
    private AtomicInteger lastSureSent = new AtomicInteger(0);
    private AtomicInteger lastOkSent = new AtomicInteger(0);
    private AtomicInteger lastOkReceived = new AtomicInteger(this.connectionTime());
    private AtomicInteger lastDataReceived = new AtomicInteger(0);

    private AtomicInteger synOkCounter = new AtomicInteger(0);

    private AtomicInteger receiverBuffer;

    private ConcurrentSkipListMap<Integer,Integer> sentOkCache = new ConcurrentSkipListMap<>();

    private AtomicInteger congestionWindowSize = new AtomicInteger(GCVConnection.initial_window_size);

    private AtomicBoolean congestionControl = new AtomicBoolean(false);

    private int initiate = 0;

    private final int maxWindow;

    public FlowWindow( int maxWindow ){
        receiverBuffer = new AtomicInteger(maxWindow);
        this.maxWindow = maxWindow;
    }

    public int congestionWindowValue(){
        int win = congestionWindowSize.get();
        int buffsize = this.receiverBuffer.get();

        return (win < buffsize? win : buffsize);
    }

    public int getMaxWindowSize(){
        return maxWindow;
    }

    void activateCongestionControl(){
        this.congestionControl.set(true);
    }

    void boot(int lastOkSent, int lastOkReceived, int time){
        this.lastOkSent.set(lastOkSent);
        this.lastOkReceived.set(lastOkReceived);
        this.lastSureReceived.set(lastOkSent);
        this.initiate = time;
    }

    public int rtt(){
        return rtt.get();
    }

    public boolean rttHasPassed(){
        return (this.connectionTime() - this.timeLastSent.get() > this.rtt.get());
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

    void receivedSure(SURE packet){

        int seq = packet.getOK();
        int curTime = connectionTime();

        if( seq != -1){
            Integer timestamp = sentOkCache.get(seq);

            sentOkCache.headMap(seq , false).clear();

            if (timestamp != null){
                int sRtt = curTime - timestamp;

                rtt.set((int)((1 - GCVConnection.rrt_factor) * rtt.get() + GCVConnection.rrt_factor * sRtt));

                rttVar.set((int)((1 - GCVConnection.var_rrt_factor) * rttVar.get()
                        + GCVConnection.var_rrt_factor * Math.abs(sRtt - rtt.get())));
            }

            this.lastSureReceived.getAndUpdate(x -> (x > seq) ? x : seq );
            this.timeLastSureReceived.set( curTime );

        }else{
            rtt.set( this.connectionTime() - initiate );
            rttVar.set(0);

            System.out.println( "Start rtt " + this.rtt);
            this.deactivateCongestionControl();
        }

        //System.out.println("rtt: " + this.rtt.get() + " var: " + this.rttVar.get() +  " ");
    }

    void sentSure( SURE packet ){

        int ok = packet.getOK();

        this.lastSureSent.getAndUpdate(x -> (x > ok) ? x : ok );
        this.timeLastSureSent.set(this.connectionTime());
    }

    void receivedOk(OK packet) {

        int seq = packet.getSeq();
        this.timeLastOkReceived.set(this.connectionTime());
        int lastseq = this.lastOkReceived.get();

        this.setRtt(packet.getRtt());
        this.setRttVar(packet.getRttVar());
        this.setReceiverBuffer(packet.getWindow());

        System.out.println("receivedOK " + packet.getSeq());

        if( lastseq == seq ){
            this.multiplicativeDecrease();
        }

        if( (seq == this.lastOkReceived.updateAndGet(x -> (x > seq) ? x : seq )) ){

            this.synOkCounter.getAndAdd( seq - lastseq );

            //int buffsize = this.receiverBuffer.get();
            //int win = this.congestionWindowSize.getAndUpdate(x -> (x < buffsize) ? x : buffsize);
            /* trys to not pass the window*/
            if (this.congestionWindowSize.get() == maxWindow)
                this.activateCongestionControl();
        }

    }

    void sentOk(OK packet){
        sentOkCache.put( packet.getSeq(), packet.getTimestamp() );
        this.lastOkSent.getAndUpdate(x -> (x > packet.getSeq() ) ? x : packet.getSeq() );
    }

    void receivedNope( NOPE packet ){
        timeLastNackReceived.set(this.connectionTime());
        this.activateCongestionControl();
    }

    void sentNope( NOPE packet ){
        System.out.println(" Sent Nope ");
        timeLastNackSent.set(this.connectionTime());
    }

    void receivedData(DataPacket packet){
        this.lastDataReceived.updateAndGet( x -> ( x > packet.getSeq() ) ? x : packet.getSeq() );
    }

    void receivedTransmission(){ this.timeLastReceived.set(this.connectionTime()); }

    void sentTransmission(){ this.timeLastSent.set(this.connectionTime()); }

    boolean okMightHaveBeenLost(){
        try{
            int expRttTime = this.rtt.get() + 4 * this.rttVar.get();
            int waitAndSendTime =  expRttTime > GCVConnection.rate_control_interval + expRttTime/2 ? expRttTime : GCVConnection.rate_control_interval + expRttTime/2;

            return (this.connectionTime()-this.timeLastSureReceived.get()) > waitAndSendTime;
        }catch(NullPointerException e){
            return false;
        }
    }

    boolean shouldSendNope(){
        int curTime = connectionTime();

        int expRttTime = this.rtt.get() + 4 * this.rttVar.get();
        System.out.println(expRttTime);
        int compound =  expRttTime > GCVConnection.rate_control_interval ? expRttTime: GCVConnection.rate_control_interval;
        int waitAndSendTime =  expRttTime> GCVConnection.rate_control_interval + expRttTime/2 ? expRttTime : GCVConnection.rate_control_interval + expRttTime/2;

        try {
            return (curTime - timeLastNackSent.get()) > waitAndSendTime
                    && (curTime - this.sentOkCache.get(this.lastOkSent.get())) > compound;
        }catch(NullPointerException e){
            return false;
        }

    }

    public int connectionTime(){ return (int)this.connectionStartTime.until(LocalDateTime.now(), ChronoUnit.MICROS); }

    int getLastSentOk(){
        return this.lastOkSent.get();
    }

    int getLastReceivedOk( ){ return this.lastOkReceived.get(); }

    int getLastSentSure() { return this.lastSureSent.get(); }

    int getLastReceivedData(){ return this.lastDataReceived.get(); }

    void syn(){
        int synCounter = this.synOkCounter.getAndSet(0);
        System.out.println("counter " + synCounter);
        System.out.println("rtt : " + this.rtt.get() );
        System.out.println("rttVar : " + this.rttVar.get() );

        if( congestionControl.get() ) {
            int expRttTime = this.rtt.get() + 4 * this.rttVar.get();
            System.out.println(expRttTime);
            expRttTime =  expRttTime > GCVConnection.rate_control_interval + expRttTime/2 ? expRttTime : GCVConnection.rate_control_interval + expRttTime/2;
            if ((this.connectionTime() - this.timeLastOkReceived.get()) > expRttTime ) {
                /* mul decrease */
                System.out.println("Multiplicative Decrease");
                this.multiplicativeDecrease();


            }else{
                /* add increase */
                System.out.println("Additive Increase");
                this.congestionWindowSize.addAndGet( synCounter > 0 ? 1 : 0);
            }

        }else{
            System.out.println("###########################");

            this.congestionWindowSize.getAndAdd( synCounter );

        }

        System.out.println("window : " + this.congestionWindowSize.get() + " \n ..........-------........." );

    }

    public float uploadSpeed(){ // Mb/s
        return GCVConnection.stdmtu * (float)this.congestionWindowValue() * 10/ 1000000;
    }

    private void multiplicativeDecrease(){
        this.congestionWindowSize.updateAndGet( x -> ( x > 2 ) ? (int)(GCVConnection.decrease_factor * x) : 2 );
    }

    boolean hasTimeout(){
        int difs = this.connectionTime() - this.timeLastReceived.get();

        int exptime = rtt.get() + 4 * rttVar.get();

        exptime =  exptime > GCVConnection.rate_control_interval ? exptime : GCVConnection.rate_control_interval + exptime/2;

        return (difs > exptime );
    }

    void deactivateCongestionControl(){
        this.congestionControl.set(false);
    }
}
