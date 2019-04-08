package Transport.Start;

import java.net.InetAddress;

public class SenderProperties extends TransportStationProperties {

    private final int stock;
    private final int level;

    private volatile long roundTripTime;
    private volatile long roundTripTimeVariance;
    private volatile long packetArrivalRate;
    private volatile long estimatedLinkCapacity;
    private volatile double sendPeriod;
    private volatile long congestionWindowSize;

    public SenderProperties(InetAddress ip, int port, int maxpacket, int maxwindow, int stock,int level){
        super(ip,port,maxpacket,maxwindow);
        this.stock = stock;
        this.level = level;
    }

    public int getLevel(){
        return this.level;
    }

    public int getStock(){
        return this.stock;
    }
}
