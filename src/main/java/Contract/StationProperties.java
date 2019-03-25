package Contract;

import java.net.InetAddress;

import Estado.ConnectionType;

public final class StationProperties {
    private final int capacity;
    private final int port;
    private final ConnectionType ct;
    private final InetAddress ip;

    public StationProperties(InetAddress ip, int capacity, int port, ConnectionType ct){
        this.ct = ct;
        this.port = port;
        this.capacity = capacity;
        this.ip = ip;
    }


    public int capacity() {
        return capacity;
    }

    public int port() {
        return port;
    }

    public ConnectionType connectionType() {
        return ct;
    }

    public InetAddress ip() {
        return ip;
    }
}
