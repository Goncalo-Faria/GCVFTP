package Transport.Start;

import Transport.Socket;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Connector {
    Socket bind(String ip) throws IOException, TimeoutException;
}
