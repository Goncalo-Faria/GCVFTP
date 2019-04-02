package Transport;

import AgenteUDP.StreamIN;
import AgenteUDP.StreamOUT;
import AgenteUDP.TransmissionChannel;
import Estado.ConnectionType;

import java.net.*;


public class GCVListener implements Listener {

    private int capacity;
    private InetAddress ip;
    private int inc;
    private int outc;

    public GCVListener(int connectCapacity, int inCapacity, int outCapacity, InetAddress myip){
        this.capacity = connectCapacity;
        this.inc = inCapacity;
        this.outc = outCapacity;
        this.ip = myip;
    }

    public GCVListener(int connectCapacity, int inCapacity, int outCapacity){
        this.capacity = connectCapacity;
        this.inc = inCapacity;
        this.outc = outCapacity;
        try {
            this.ip = InetAddress.getByName("localhost");
        }catch( UnknownHostException e){
            e.getStackTrace();
        }
    }

    public Socket accept() throws SocketException {
        try {
            StreamIN ch = new StreamIN(
                    this.capacity,
                    ControlPacket.size,
                    InetAddress.getByName("localhost"),
                    GCVConnection.port);

            DatagramPacket packet = ch.getDatagram();

            InetAddress sender_ip = packet.getAddress();
            int send_port = packet.getPort();

            StationProperties sender_st = new StationProperties(
                    sender_ip,
                    this.outc,
                    send_port,
                    ConnectionType.SEND);

            InetSocketAddress sa = new InetSocketAddress(0);

            int message_port = sa.getPort();

            StationProperties receive_st = new StationProperties(
                    this.ip,
                    this.inc,
                    message_port,
                    ConnectionType.RECEIVE);

            // mandar ack com a porta e mtu

            StreamOUT sout = new StreamOUT(this.outc,
                    GCVConnection.maxdata,
                    sender_ip,
                    send_port);

            ControlPacket c = new ControlPacket();

            Socket tch = new Socket(receive_st, sout);

            return tch;

        }catch(UnknownHostException e){
            e.getStackTrace();
        }catch(InterruptedException a){
            a.getStackTrace();
        }

    }
}
