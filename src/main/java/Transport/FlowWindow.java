package Transport;

import java.util.concurrent.atomic.AtomicInteger;

public class FlowWindow {

    private volatile long roundTripTime;
    private volatile long roundTripTimeVariance;
    private volatile long packetArrivalRate;
    private volatile long estimatedLinkCapacity;
    private volatile double sendPeriod;
    private volatile long congestionWindowSize;

    private final int maxwindow;

    private AtomicInteger value = new AtomicInteger(10);

    public FlowWindow( int maxwindow){
        this.maxwindow = maxwindow;
    }

    public int value(){
        return value.get();
    }

    public int getMaxWindow(){
        return maxwindow;
    }
}
