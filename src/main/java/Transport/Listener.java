package Transport;

import Transport.Channel;

import java.net.SocketException;

public interface Listener {
    Channel accept() throws SocketException,InterruptedException;
}
