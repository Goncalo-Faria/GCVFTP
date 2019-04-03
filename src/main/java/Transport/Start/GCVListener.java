package Transport.Start;


import Transport.GCVConnection;
import Transport.Socket;
import AgenteUDP.StationProperties;
import Transport.Unit.ControlPacket;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;


public class GCVListener implements Listener {

    private int inc;
    private int outc;

    private static ConnectionScheduler common_daemon=null;

    private static void activate(){
        if( common_daemon == null){
            try {

                GCVListener.common_daemon = new ConnectionScheduler(
                        GCVConnection.connection_receive_capacity,
                        InetAddress.getByName("localhost"),
                        GCVConnection.port,
                        GCVConnection.connection_receive_ttl,
                        ControlPacket.Type.HI);

            } catch (SocketException| UnknownHostException e) {
                e.printStackTrace();
            }
        }

    }
    private static void close(){
        GCVListener.common_daemon.close();
        GCVListener.common_daemon = null;
    }

    public GCVListener(int inCapacity, int outCapacity){
        GCVListener.activate();
        this.inc = inCapacity;
        this.outc = outCapacity;

    }

    public Socket accept() throws SocketException {
        try {

            ControlPacket packet = GCVListener.common_daemon.get();/*waiting for datagram*/

            packet.startBuffer();

            int caller_port = packet.getInt();

            String caller_ip_text = packet.asString();

            InetAddress caller_ip = InetAddress.getByName(caller_ip_text);

            StationProperties caller_station_properties = new StationProperties(
                    caller_ip,
                    this.outc,
                    caller_port,
                    StationProperties.ConnectionType.SEND,
                    GCVConnection.maxcontrol);

            InetSocketAddress sa = new InetSocketAddress(0);

            int message_port = sa.getPort();

            StationProperties my_station_properties = new StationProperties(
                    InetAddress.getByName("localhost"),
                    this.inc,
                    message_port,
                    StationProperties.ConnectionType.RECEIVE,
                    GCVConnection.maxdata);

            return new Socket(my_station_properties, caller_station_properties);

        } catch (UnknownHostException|InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }


}
