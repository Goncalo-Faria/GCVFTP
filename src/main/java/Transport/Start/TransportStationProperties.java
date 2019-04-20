package Transport.Start;

import AgenteUDP.StationProperties;

import java.net.InetAddress;

public class TransportStationProperties extends StationProperties {

    private final int buffer_size;

    public TransportStationProperties(InetAddress ip,  int port,  int maxpacket, int buffer_size){
        super(ip,port,maxpacket, buffer_size*maxpacket);
        this.buffer_size = buffer_size;
    }

    public int transmissionchannel_buffer_size(){
        return this.buffer_size;
    }


}
