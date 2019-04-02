package Transport.Start;


import Estado.ConnectionType;
import Transport.GCVConnection;
import Transport.Socket;
import Transport.StationProperties;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;


public class GCVListener implements Listener {

    private int inc;
    private int outc;

    private static ConnectionScheduler common_daemon=null;

    private static void activate(){
        if( common_daemon == null){
            try {

                GCVListener.common_daemon = new ConnectionScheduler(
                        GCVConnection.connection_receive_capacity,
                        GCVConnection.maxcontrol,
                        InetAddress.getByName("localhost"),
                        GCVConnection.port,
                        GCVConnection.connection_receive_ttl);

            } catch (SocketException| UnknownHostException e) {
                e.printStackTrace();
            }
        }

    }
    private static void close(){
        GCVListener.common_daemon.close();
    }

    public GCVListener(int inCapacity, int outCapacity){
        this.inc = inCapacity;
        this.outc = outCapacity;

    }

    public Socket accept() throws SocketException {
        try {

            DatagramPacket packet = GCVListener.common_daemon.get();/*waiting for datagram*/

            InetAddress caller_ip = packet.getAddress();
            int caller_port = packet.getPort();

            StationProperties caller_station_properties = new StationProperties(
                    caller_ip,
                    this.outc,
                    caller_port,
                    ConnectionType.SEND);

            InetSocketAddress sa = new InetSocketAddress(0);

            int message_port = sa.getPort();

            StationProperties my_station_properties = new StationProperties(
                    InetAddress.getByName("localhost"),
                    this.inc,
                    message_port,
                    ConnectionType.RECEIVE);

            return new Socket(my_station_properties, caller_station_properties);


        } catch (UnknownHostException|InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }


}
