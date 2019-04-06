package Transport;

import Transport.Unit.Packet;

import java.io.IOException;

public interface TransportChannel {
    void send( Packet packet) throws IOException;
    Packet receive() throws IOException;
}
