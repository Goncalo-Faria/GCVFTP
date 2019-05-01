package Transport.Sender;

import Transport.TransportStationProperties;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;


public class SenderProperties extends TransportStationProperties {



    private AtomicBoolean persistent;

    public SenderProperties(InetAddress ip, int port, int maxpacket, int buffer, boolean isPersistent){
        super(ip,port,maxpacket, buffer);
        this.persistent = new AtomicBoolean(isPersistent);

    }

    public boolean isPersistent(){
        return persistent.get();
    }

}
