package Transport.Start;

import AgenteUDP.StreamIN;
import AgenteUDP.StreamOUT;
import Estado.ConnectionType;
import Transport.GCVConnection;
import Transport.Socket;
import Transport.StationProperties;
import Transport.Unit.ControlPacket;

import java.net.*;


public class GCVListener implements Listener {

    private int inc;
    private int outc;

    private static ConnectionScheduler common_daemon=null;

    private static void activate(){
        if( common_daemon == null){
            try {

                GCVListener.common_daemon = new ConnectionScheduler(
                        GCVConnection.connection_request_capacity,
                        GCVConnection.maxcontrol,
                        InetAddress.getByName("localhost"),
                        GCVConnection.port,
                        GCVConnection.connection_request_ttl);

            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
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

            StationProperties caller_st = new StationProperties(
                    caller_ip,
                    this.outc,
                    caller_port,
                    ConnectionType.SEND);

            InetSocketAddress sa = new InetSocketAddress(0);

            int message_port = sa.getPort();

            StationProperties receive_st = new StationProperties(
                    InetAddress.getByName("localhost"),
                    this.inc,
                    message_port,
                    ConnectionType.RECEIVE);

            StreamOUT sout = new StreamOUT(this.outc,
                    GCVConnection.maxcontrol,
                    caller_ip,
                    caller_port);

            ControlPacket c = new ControlPacket("ACK");

            Transport.Socket tch = new Socket(receive_st, sout);

            return tch;

        }catch(UnknownHostException e){
            e.getStackTrace();
        }catch(InterruptedException a){
            a.getStackTrace();
        }

    }


}
