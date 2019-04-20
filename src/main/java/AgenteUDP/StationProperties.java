package AgenteUDP;

import java.net.InetAddress;

public class StationProperties {
    private int port;
    private final InetAddress ip;
    private final int maxpacket;
    private final int bufferSize;

    public StationProperties(InetAddress ip,  int port,  int maxpacket, int bufferSize){

        this.port = port;
        this.ip = ip;
        this.maxpacket = maxpacket;
        this.bufferSize = bufferSize;
    }

    public int port() {
        return port;
    }

    public InetAddress ip() {
        return ip;
    }

    public void setPort(int port){ this.port = port; }

    public int packetsize(){ return this.maxpacket;}

    public int channel_buffer_size(){
        return this.bufferSize;
    }


}
