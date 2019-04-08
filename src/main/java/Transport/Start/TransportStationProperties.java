package Transport.Start;

import AgenteUDP.StationProperties;

import java.net.InetAddress;

public class TransportStationProperties extends StationProperties {
    private final int maxwindow;

    public TransportStationProperties(InetAddress ip,  int port,  int maxpacket, int maxwindow){
        super(ip,port,maxpacket);
        this.maxwindow = maxwindow;
    }

    public int window(){ return this.maxwindow; }
}
