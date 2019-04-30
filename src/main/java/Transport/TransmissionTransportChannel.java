package Transport;

import AgenteUDP.TransmissionChannel;
import Transport.ControlPacketTypes.NOPE;
import Transport.ControlPacketTypes.OK;
import Transport.ControlPacketTypes.SURE;
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

        if( p instanceof SURE){
            this.window.sentSure((SURE)p);
        }else if( p instanceof NOPE){
            this.window.sentNope((NOPE)p);
        }else if( p instanceof OK){
            this.window.sentOk((OK)p);
        }

        this.send(p.serialize());
    }

    public Packet receivePacket() throws IOException {
        this.window.receivedTransmission();
        Packet p = Packet.parse(super.receive());

        if( p instanceof SURE){
            this.window.receivedSure((SURE)p);
        }else if( p instanceof NOPE){
            this.window.receivedNope((NOPE)p);
        }else if( p instanceof OK ){
            this.window.receivedOk((OK)p);
        }

        return p;
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
