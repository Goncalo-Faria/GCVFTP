package Transport.Start;

import Transport.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

public interface Connector {
    Socket bind(String ip) throws InterruptedException, UnknownHostException, SocketException, TimeoutException;
}
