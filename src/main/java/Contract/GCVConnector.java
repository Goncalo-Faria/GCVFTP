package Contract;

import Contract.Channel;

public interface GCVConnector {
    Channel connect(String ip, int port) throws InterruptedException;
}
