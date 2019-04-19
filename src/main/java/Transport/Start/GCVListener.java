package Transport.Start;


import Transport.ControlPacketTypes.HI;
import Transport.GCVConnection;
import Transport.Sender.SenderProperties;
import Transport.Receiver.ReceiverProperties;
import Transport.Socket;
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
                        HI.size);

            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
    }

    private int mtu = GCVConnection.stdmtu;
    private int maxwindow = 1024*8;
    private int stock = 10;
    private InetAddress localhost;

    public GCVListener(){
        GCVListener.activate();
    }

    static public void announceConnection(String key, Socket cs){
        if( common_daemon != null)
            GCVListener.common_daemon.announceConnection(key,cs);

    }

    static public void closeConnection(String key){
        if( common_daemon != null)
            GCVListener.common_daemon.closeConnection(key);

    }

    public GCVListener( int mtu, int maxwindow, int sendingstock) throws UnknownHostException{
        this.mtu = mtu;
        this.maxwindow = maxwindow;
        this.stock = sendingstock;
        this.localhost = InetAddress.getLocalHost();
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
                    this.localhost,
                    message_port,
                    this.mtu,
                    this.maxwindow,
                    this.stock);

            ReceiverProperties caller_station_properties = new ReceiverProperties(
                    caller_ip,
                    caller_port,
                    packet.getMTU(),
                    packet.getMaxWindow());

            System.out.println("Almost there");

            Socket cs = new Socket(my_station_properties, caller_station_properties, packet.getSeq());

            announceConnection(caller_ip.toString() + caller_port, cs);

            return cs;

    }

    public void close(){
        GCVListener.common_daemon.close();
        GCVListener.common_daemon = null;
    }


}
