package Transport.Start;

import Transport.FlowWindow;

import java.net.InetAddress;


public class SenderProperties extends TransportStationProperties {

    private final int stock;
    private FlowWindow  window = new FlowWindow();

    public SenderProperties(InetAddress ip, int port, int maxpacket, int maxwindow, int stock){
        super(ip,port,maxpacket,maxwindow);
        this.stock = stock;
    }

    public int getBufferSize(){
        return this.stock;
    }
}
