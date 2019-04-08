package Transport;

import AgenteUDP.StationProperties;
import AgenteUDP.TransmissionChannel;
import Transport.Start.TransportStationProperties;
import Transport.Unit.Packet;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TransmissionTransportChannel extends TransmissionChannel implements TransportChannel {

    public TransmissionTransportChannel(
            TransportStationProperties in,
            TransportStationProperties out) throws SocketException {
        super(in,out);
    }

    public TransmissionTransportChannel(DatagramSocket cs,
                                        TransportStationProperties in,
                                        TransportStationProperties out) throws SocketException {
        super(cs,in,out);
    }

    public void sendPacket(Packet p) throws IOException {

        this.send(p.serialize());
    }

    public Packet receivePacket() throws IOException{

        return Packet.parse(super.receive());
    }

    public int inMTU(){
        return this.getinStationProperties().packetsize();
    }

    public int outMTU(){
        return this.getoutStationProperties().packetsize();
    }

    public TransportStationProperties getSelfStationProperties(){
        return (TransportStationProperties)this.getinStationProperties();
    }

    public TransportStationProperties getOtherStationProperties(){
        return (TransportStationProperties)this.getoutStationProperties();
    }

}
