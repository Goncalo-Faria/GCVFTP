package Transport.Start;

import Transport.ControlPacketTypes.HI;
import Transport.GCVConnection;
import Transport.Sender.SenderProperties;
import Transport.Socket;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;


import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GCVConnector implements Connector {

    private byte[] connection_message;
    private AtomicBoolean active = new AtomicBoolean(true);

    private SenderProperties in_properties;

    public GCVConnector(int my_port, int max_window) {
        this(my_port,GCVConnection.stdmtu, max_window);
    }

    public GCVConnector(int my_port, int mtu, int max_window){
        try {
            this.in_properties = new SenderProperties(
                    InetAddress.getLocalHost(),
                    my_port,
                    mtu,
                    max_window,
                    GCVConnection.send_buffer_size);

            HI p = new HI((short)0,0,mtu,max_window);

            this.connection_message = p.serialize();
        }catch(UnknownHostException e){
            e.getStackTrace();
        }

    }

    public Socket bind(String ip) throws IOException, TimeoutException{

        return this.bind(InetAddress.getByName(ip));

    }

    public Socket bind(InetAddress ip) throws IOException, TimeoutException {

        this.active.set(true);

        DatagramPacket send_message = new DatagramPacket(
                connection_message,0,
                connection_message.length,
                ip,
                GCVConnection.port);

        DatagramPacket received_message = new DatagramPacket(
                new byte[HI.size],
                HI.size);

        DatagramSocket cs = new DatagramSocket(this.in_properties.port());
        cs.setSoTimeout(GCVConnection.request_retry_timeout);

        int tries = 0;
        while(this.active.get() && tries < GCVConnection.request_retry_number ) {

            System.out.println("sent " + connection_message.length + " bytes");
            cs.send(send_message);
            try {
                System.out.println(":localport " + cs.getLocalPort() );
                cs.receive(received_message);
                Packet du = Packet.parse(received_message.getData());
                if(du instanceof ControlPacket){
                    ControlPacket cdu = (ControlPacket)du;

                    if( cdu instanceof HI ){
                        HI hi = (HI)cdu;
                        this.active.set(false);
                        TransportStationProperties out_properties = new TransportStationProperties(
                                ip,
                                received_message.getPort(),
                                hi.getMTU(),
                                GCVConnection.receive_buffer_size
                                );

                        return new Socket(cs,this.in_properties, out_properties, hi.getSeq());
                    }
                }

            }catch (SocketTimeoutException ste){
                tries++;
            }

        }

        throw new TimeoutException();

    }

}
