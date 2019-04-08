package AgenteUDP;

import Transport.Unit.Packet;
import java.io.IOException;

public interface Channel {
    void send( byte[] data) throws IOException;
    byte[] receive() throws IOException;
    void close();
}
