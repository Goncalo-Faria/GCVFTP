package Transport.Impl;

import Test.Debugger;
import Transport.GCVConnection;
import Transport.Unit.ControlPacketTypes.NOPE;
import Transport.Unit.ControlPacketTypes.OK;
import Transport.Unit.ControlPacketTypes.SURE;
import Transport.Unit.DataPacket;
import Transport.Window;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowWindow implements Window {

    private final LocalDateTime connectionStartTime = LocalDateTime.now();

    private final AtomicInteger rtt = new AtomicInteger(100);
    private final AtomicInteger rttVar =  new AtomicInteger(100);


    private final AtomicInteger timeLastReceived = new AtomicInteger(this.connectionTime());
    private final AtomicInteger timeLastSent = new AtomicInteger(0);

    private final AtomicInteger timeLastNackSent = new AtomicInteger(0);
    private final AtomicInteger timeLastSureSent = new AtomicInteger(0);

    private final AtomicInteger timeLastNackReceived = new AtomicInteger(0);
    private final AtomicInteger timeLastSureReceived = new AtomicInteger(0);
    private final AtomicInteger timeLastOkReceived = new AtomicInteger(0);

    private final AtomicInteger lastSureReceived = new AtomicInteger(0);
    private final AtomicInteger lastSureSent = new AtomicInteger(0);
    private final AtomicInteger lastOkSent = new AtomicInteger(0);
    private final AtomicInteger lastOkReceived = new AtomicInteger(this.connectionTime());
    private final AtomicInteger lastDataReceived = new AtomicInteger(0);

    private final AtomicInteger synOkCounter = new AtomicInteger(0);
    private final AtomicInteger increaseCounter = new AtomicInteger(0);

    private final AtomicInteger receiverBuffer;

    private final ConcurrentSkipListMap<Integer,Integer> sentOkCache = new ConcurrentSkipListMap<>();

    private final AtomicInteger congestionWindowSize = new AtomicInteger(GCVConnection.initial_window_size);

    private final AtomicBoolean congestionControl = new AtomicBoolean(false);

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

    private void activateCongestionControl(){
        this.congestionControl.set(true);
    }

    public void boot(int lastOkSent, int lastOkReceived, int time){
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

    public void addRtt(int sRtt){
        this.rtt.set((int)((1 - GCVConnection.rrt_factor) * rtt.get() + GCVConnection.rrt_factor * sRtt));
        this.rttVar.set((int)((1 - GCVConnection.var_rrt_factor) * rttVar.get()
                + GCVConnection.var_rrt_factor * Math.abs(sRtt - rtt.get())));
    }

    private void setReceiverBuffer(int window){
        this.receiverBuffer.set(window);
    }

    public int rttVar(){
        return rttVar.get();
    }

    public void receivedSure(SURE packet){

        int seq = packet.getOK();
        int curTime = connectionTime();

        if( seq != -1){
            Integer timestamp = sentOkCache.get(seq);

            SortedMap<Integer,Integer> readyToRemove = sentOkCache.headMap(seq , false);

            if( !readyToRemove.isEmpty() ) {
            /*    readyToRemove.remove(readyToRemove.firstKey());
                readyToRemove.forEach((key, value) -> {
                    this.addRtt(curTime - value);
                });
                */
                readyToRemove.clear();
            }


            if (timestamp != null){
                int sRtt = curTime - timestamp;
                this.addRtt( sRtt );
            }

            this.lastSureReceived.getAndUpdate(x -> (x > seq) ? x : seq );
            this.timeLastSureReceived.set( curTime );

        }else{
            this.rtt.set( this.connectionTime() - initiate );
            this.rttVar.set(0);

            Debugger.log( "Start rtt " + this.rtt);
            this.deactivateCongestionControl();
        }
    }

    public void sentSure( SURE packet ){

        int ok = packet.getOK();

        this.lastSureSent.getAndUpdate(x -> (x > ok) ? x : ok );
        this.timeLastSureSent.set(this.connectionTime());
    }

    public void receivedOk(OK packet) {

        int seq = packet.getSeq();
        this.timeLastOkReceived.set(this.connectionTime());
        int lastseq = this.lastOkReceived.get();

        this.rtt.set(packet.getRtt());
        this.rttVar.set(packet.getRttVar());

        this.setReceiverBuffer(packet.getWindow());

        Debugger.log("receivedOK " + packet.getSeq());

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

    public void sentOk(OK packet){
        sentOkCache.put( packet.getSeq(), this.connectionTime());
        this.lastOkSent.getAndUpdate(x -> (x > packet.getSeq() ) ? x : packet.getSeq() );
    }

    public void receivedNope( NOPE packet ){
        timeLastNackReceived.set(this.connectionTime());
        this.activateCongestionControl();
    }

    public void sentNope( NOPE packet ){
        Debugger.log(" Sent Nope ");
        timeLastNackSent.set(this.connectionTime());
    }

    public void receivedData(DataPacket packet){
        this.lastDataReceived.updateAndGet( x -> ( x > packet.getSeq() ) ? x : packet.getSeq() );
    }

    public void receivedTransmission(){ this.timeLastReceived.set(this.connectionTime()); }

    public void sentTransmission(){ this.timeLastSent.set(this.connectionTime()); }

    public boolean okMightHaveBeenLost(){
        try{
            int expRttTime = this.rtt.get() + 4 * this.rttVar.get();
            int waitAndSendTime =  expRttTime > GCVConnection.rate_control_interval + expRttTime ? expRttTime : GCVConnection.rate_control_interval + expRttTime;

            return (this.connectionTime()-this.timeLastSureReceived.get()) > waitAndSendTime;
        }catch(NullPointerException e){
            return false;
        }
    }

    public boolean shouldSendOk(){
        return this.lastOkSent.get() < this.lastDataReceived.get();
    }

    public boolean shouldSendSure(){
        return this.lastOkReceived.get() > this.lastSureSent.get();
    }

    public boolean shouldSendNope(){
        int curTime = connectionTime();

        int expRttTime = this.rtt.get() + this.rttVar.get();
        int compound =  expRttTime > GCVConnection.rate_control_interval ? expRttTime: GCVConnection.rate_control_interval;
        int waitAndSendTime =  expRttTime> GCVConnection.rate_control_interval + expRttTime ? expRttTime : GCVConnection.rate_control_interval + expRttTime;

        try {
            return (curTime - timeLastNackSent.get()) > waitAndSendTime
                    && (curTime - this.sentOkCache.get(this.lastOkSent.get())) > compound;
        }catch(NullPointerException e){
            return false;
        }
    }

    public int connectionTime(){ return (int)this.connectionStartTime.until(LocalDateTime.now(), ChronoUnit.MICROS); }

    public int lastOkSent(){
        return this.lastOkSent.get();
    }

    public int lastOkReceived( ){ return this.lastOkReceived.get(); }

    public int lastSureSent() { return this.lastSureSent.get(); }

    public int lastDataReceived(){ return this.lastDataReceived.get(); }

    public void syn(){
        int synCounter = this.synOkCounter.getAndSet(0);
        Debugger.log("counter " + synCounter);
        Debugger.log("rtt : " + this.rtt.get() );
        Debugger.log("rttVar : " + this.rttVar.get() );

        if( congestionControl.get() ) {
            int expRttTime = this.rtt.get() + 4 * this.rttVar.get();
            expRttTime =  GCVConnection.rate_control_interval + expRttTime;
            if ((this.connectionTime() - this.timeLastOkReceived.get()) > expRttTime ) {
                /* mul decrease */
                Debugger.log("Multiplicative decrease");
                this.multiplicativeDecrease();
                this.increaseCounter.set(0);


            }else{
                /* add increase */
                Debugger.log("Additive increase");
                this.congestionWindowSize.addAndGet( synCounter > 0 ? 1 : 0);

                int inc = this.increaseCounter.incrementAndGet();

                if(  inc  > 20 )
                    this.deactivateCongestionControl();

                if( synCounter > 2 * this.congestionWindowSize.get() )
                    this.deactivateCongestionControl();
            }

        }else{
            Debugger.log("Send like a crazy person");

            this.congestionWindowSize.getAndAdd( synCounter );
        }

        Debugger.log("window : " + this.congestionWindowValue() + " \n ..................." );
        Debugger.log( "buffer : " + this.receiverBuffer.get());

    }

    public float uploadSpeed(){ // Mb/s
        return GCVConnection.stdmtu * (float)this.congestionWindowValue() * 10/ 1000000;
    }

    private void multiplicativeDecrease(){
        int buff = this.receiverBuffer.get();
        this.congestionWindowSize.updateAndGet(
                x -> ( x > 2 ) ? (
                        x > buff ? (int)(GCVConnection.decrease_factor * buff) : (int)(GCVConnection.decrease_factor * x)
                ) : 2
        );
    }

    public boolean hasTimeout(){
        int difs = this.connectionTime() - this.timeLastReceived.get();

        int exptime = rtt.get() + 4 * rttVar.get();

        return (difs > 4*( GCVConnection.rate_control_interval + exptime));
    }

    private void deactivateCongestionControl(){
        this.congestionControl.set(false);
    }
}
