package Transport.Receiver;

import Transport.Start.TransportStationProperties;

import java.net.InetAddress;

public class ReceiverProperties extends TransportStationProperties {

    public ReceiverProperties(InetAddress ip, int port, int maxpacket, int buffer){
        super(ip,port,maxpacket, buffer);
    }
}
