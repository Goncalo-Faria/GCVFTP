package Transport.Start;

import AgenteUDP.Channel;
import AgenteUDP.StreamOUT;

import java.net.InetAddress;

public class GCVConnector implements Connector {


    public GCVConnector(){

    }

    public Channel connect(String ip) throws InterruptedException {
        new StreamOUT(capacity, packetSize, InetAddress.getByName(ip), int port);

    }
}
