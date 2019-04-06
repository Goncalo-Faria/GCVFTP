package Transport;

import AgenteUDP.StationProperties;
import AgenteUDP.TransmissionChannel;
import Transport.Unit.Packet;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TransmissionTransportChannel implements TransportChannel {

    private TransmissionChannel ch;

    public TransmissionTransportChannel(
            StationProperties in,
            StationProperties out) throws SocketException {
        this.ch = new TransmissionChannel(in,out);
    }

    public TransmissionTransportChannel(DatagramSocket cs,
                                        StationProperties in,
                                        StationProperties out) throws SocketException {
        this.ch = new TransmissionChannel(cs,in,out);
    }

    public void send(Packet p) throws IOException {
        this.ch.send(p.serialize());
    }

    public Packet receive() throws IOException{

        return Packet.parse(this.ch.receive());
    }
}
