package AgenteUDP;

import java.net.InetAddress;

public final class StationProperties {
    private final int capacity;
    private final int port;
    private final ConnectionType ct;
    private final InetAddress ip;
    private final int maxpacket;

    public enum ConnectionType {
        SEND,
        RECEIVE
    }

    public StationProperties(InetAddress ip, int capacity, int port, ConnectionType ct, int maxpacket){
        this.ct = ct;
        this.port = port;
        this.capacity = capacity;
        this.ip = ip;
        this.maxpacket = maxpacket;
    }


    public int capacity() {
        return capacity;
    }

    public int port() {
        return port;
    }

    public ConnectionType connectionType() {
        return ct;
    }

    public InetAddress ip() {
        return ip;
    }

    public int packetsize(){ return this.maxpacket;}
}
