package Transport.Sender;

import Transport.FlowWindow;
import Transport.Start.TransportStationProperties;

import java.net.InetAddress;


public class SenderProperties extends TransportStationProperties {


    private FlowWindow  window;

    public SenderProperties(InetAddress ip, int port, int maxpacket, int maxwindow, int buffer){
        super(ip,port,maxpacket, buffer);
        this.window = new FlowWindow(maxwindow);

    }

    public FlowWindow window(){
        return this.window;
    }

}
