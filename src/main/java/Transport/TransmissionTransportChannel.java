package Transport;

import AgenteUDP.TransmissionChannel;
import Transport.Unit.Packet;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TransmissionTransportChannel extends TransmissionChannel implements TransportChannel {

    private FlowWindow window;

    public TransmissionTransportChannel(
            TransportStationProperties send,
            TransportStationProperties receive,
            FlowWindow window) throws SocketException {
        super(send,receive);
        this.window = window;
    }

    public TransmissionTransportChannel(DatagramSocket cs,
                                        TransportStationProperties send,
                                        TransportStationProperties receive,
                                        FlowWindow window
                                        ) throws SocketException {
        super(cs,send,receive);
        this.window = window;
    }

    public void sendPacket(Packet p) throws IOException {
        this.window.sentTransmission();
        this.send(p.serialize());
    }

    public Packet receivePacket() throws IOException{
        this.window.gotTransmission();
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
