package Transport.Start;

import AgenteUDP.Channel;

public interface Connector {
    Channel connect(String ip) throws InterruptedException;
}
