package Transport.Start;

import Transport.GCVConnection;
import Transport.Socket;
import AgenteUDP.StationProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;


import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GCVConnector implements Connector {

    private byte[] connection_message;
    private AtomicBoolean active = new AtomicBoolean(true);

    private DatagramSocket cs;
    private StationProperties in_properties;
    private StationProperties out_properties;

    public GCVConnector(int my_port){
        try {
            this.in_properties = new StationProperties(
                    InetAddress.getLocalHost(),
                    my_port,
                    GCVConnection.maxcontrol);

            ControlPacket p = ControlPacket.hi(0);

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
                new byte[ControlPacket.header_size],
                ControlPacket.header_size);

        this.cs = new DatagramSocket(this.in_properties.port());
        this.cs.setSoTimeout(GCVConnection.request_retry_timeout);
        //cs.connect(ip,GCVConnection.port);


        int tries = 0;
        while(this.active.get() && tries < GCVConnection.request_retry_number ) {

            System.out.println("sent " + connection_message.length + " bytes");
            this.cs.send(send_message);
            try {
                System.out.println(":localport " + this.cs.getLocalPort() );
                this.cs.receive(received_message);
                System.out.println("--got in--");
                Packet du = Packet.parse(received_message.getData());
                if(du instanceof ControlPacket){
                    ControlPacket cdu = (ControlPacket)du;
                    if( cdu.getType().equals(ControlPacket.Type.OK) ){
                        this.active.set(false);
                        this.out_properties = new StationProperties(
                                    ip,
                                    received_message.getPort(),
                                    GCVConnection.maxdata);
                        System.out.println(cdu.serialize().length);
                        return new Socket(this.cs,this.in_properties,this.out_properties);
                    }
                }



            }catch (SocketTimeoutException ste){
                tries++;
            }

        }

        throw new TimeoutException();

    }

}
