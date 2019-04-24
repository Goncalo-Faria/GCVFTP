package Transport;

import AgenteUDP.TransmissionChannel;
import Transport.Start.TransportStationProperties;
import Transport.Unit.Packet;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TransmissionTransportChannel extends TransmissionChannel implements TransportChannel {

    public TransmissionTransportChannel(
            TransportStationProperties send,
            TransportStationProperties receive) throws SocketException {
        super(send,receive);
    }

    public TransmissionTransportChannel(DatagramSocket cs,
                                        TransportStationProperties send,
                                        TransportStationProperties receive
                                        ) throws SocketException {
        super(cs,send,receive);
    }

    public void sendPacket(Packet p) throws IOException {

        this.send(p.serialize());
    }

    public Packet receivePacket() throws IOException{

        return Packet.parse(super.receive());
    }

    public int inMTU(){
        return this.getinStationProperties().mtu();
    }

    public int outMTU(){
        return this.getoutStationProperties().mtu();
    }

    public TransportStationProperties getSelfStationProperties(){
        return (TransportStationProperties)this.getinStationProperties();
    }

    public TransportStationProperties getOtherStationProperties(){
        return (TransportStationProperties)this.getoutStationProperties();
    }

}
