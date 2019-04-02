package Transport.Start;

import AgenteUDP.Channel;

import java.net.SocketException;

public interface Listener {
    Channel accept() throws SocketException,InterruptedException;
}
