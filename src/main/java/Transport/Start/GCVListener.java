package Transport.Start;


import Transport.ControlPacketTypes.HI;
import Transport.GCVConnection;
import Transport.Socket;
import AgenteUDP.StationProperties;
import Transport.Unit.ControlPacket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class GCVListener implements Listener {

    private static ConnectionScheduler common_daemon=null;

    private static void activate(){
        if( common_daemon == null){
            try {
                GCVListener.common_daemon = new ConnectionScheduler(
                        GCVConnection.port,
                        GCVConnection.connection_receive_ttl,
                        ControlPacket.Type.HI,
                        HI.size);

            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private int mtu = GCVConnection.stdmtu;
    private int maxwindow = 1024*8;
    private int stock = 10;
    private int level = 5;

    public GCVListener(){
        GCVListener.activate();
    }

    public GCVListener( int mtu, int maxwindow, int sendingstock, int sendinglevel){
        this.mtu = mtu;
        this.maxwindow = maxwindow;
        this.stock = sendingstock;
        this.level = sendinglevel;
        GCVListener.activate();
    }

    public Socket accept() throws InterruptedException, IOException {


            ConnectionScheduler.StampedControlPacket packets = GCVListener.common_daemon.getstamped();
            HI packet = (HI)packets.get();/*waiting for datagram*/

            int caller_port = packets.port();

            InetAddress caller_ip = packets.ip();

            InetSocketAddress sa = new InetSocketAddress(0);

            int message_port = sa.getPort();

            System.out.println(" about to bind ");

            SenderProperties my_station_properties = new SenderProperties(
                    InetAddress.getLocalHost(),
                    message_port,
                    this.mtu,
                    this.maxwindow,
                    this.stock,
                    this.level);

            TransportStationProperties caller_station_properties = new TransportStationProperties(
                    caller_ip,
                    caller_port,
                    packet.getMTU(),
                    packet.getMaxWindow());

            System.out.println("Almost there");

            return new Socket(my_station_properties, caller_station_properties, packet.getSeq());

    }

    public void close(){
        GCVListener.common_daemon.close();
        GCVListener.common_daemon = null;
    }


}
