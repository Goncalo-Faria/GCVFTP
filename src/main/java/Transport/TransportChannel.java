package Transport;

import Transport.Unit.Packet;

import java.io.IOException;

public interface TransportChannel {
    void sendPacket( Packet packet) throws IOException;
    Packet receivePacket() throws IOException;
}
