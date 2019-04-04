package Transport.Start;


import Transport.GCVConnection;
import Transport.Socket;
import AgenteUDP.StationProperties;
import Transport.Unit.ControlPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;

public class GCVListener implements Listener {

    private static ConnectionScheduler common_daemon=null;

    private static void activate(){
        if( common_daemon == null){
            try {
                GCVListener.common_daemon = new ConnectionScheduler(
                        GCVConnection.port,
                        GCVConnection.connection_receive_ttl,
                        ControlPacket.Type.HI,
                        8 + ControlPacket.header_size);

            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private static void close(){
        GCVListener.common_daemon.close();
        GCVListener.common_daemon = null;
    }

    public GCVListener(){
        GCVListener.activate();
    }

    public Socket accept() throws InterruptedException, IOException {


            ConnectionScheduler.StampedControlPacket packets = GCVListener.common_daemon.getstamped();
            ControlPacket packet = packets.get();/*waiting for datagram*/


            int caller_port = packets.port();

            InetAddress caller_ip = packets.ip();

            StationProperties caller_station_properties = new StationProperties(
                    caller_ip,
                    caller_port,
                    GCVConnection.maxcontrol);

            InetSocketAddress sa = new InetSocketAddress(0);

            int message_port = sa.getPort();

            StationProperties my_station_properties = new StationProperties(
                    InetAddress.getLocalHost(),
                    message_port,
                    GCVConnection.maxdata);

            return new Socket(my_station_properties, caller_station_properties);


    }


}
