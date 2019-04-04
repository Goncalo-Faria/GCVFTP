package AgenteUDP;

import java.net.InetAddress;

public final class StationProperties {
    private int port;
    private final InetAddress ip;
    private final int maxpacket;

    public enum ConnectionType {
        SEND,
        RECEIVE
    }

    public StationProperties(InetAddress ip,  int port,  int maxpacket){

        this.port = port;
        this.ip = ip;
        this.maxpacket = maxpacket;
    }

    public int port() {
        return port;
    }

    public InetAddress ip() {
        return ip;
    }

    public void setPort(int port){ this.port = port; }

    public int packetsize(){ return this.maxpacket;}
}
