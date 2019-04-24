package Transport.Sender;

import Transport.FlowWindow;
import Transport.TransportStationProperties;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicBoolean;


public class SenderProperties extends TransportStationProperties {


    private FlowWindow  window;
    private AtomicBoolean persistent;

    public SenderProperties(InetAddress ip, int port, int maxpacket, int buffer, boolean isPersistent){
        super(ip,port,maxpacket, buffer);
        this.window = new FlowWindow(buffer);
        this.persistent = new AtomicBoolean(isPersistent);

    }

    public FlowWindow window(){
        return this.window;
    }

    public boolean isPersistent(){
        return persistent.get();
    }

}
