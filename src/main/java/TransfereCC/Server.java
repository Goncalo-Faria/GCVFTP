package TransfereCC;

import AgenteUDP.StreamIN;
import Estado.ConnectionState;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private StreamIN streamIN;
    private Map<Integer, ConnectionState> connectionStateMap;
    private boolean isRunning = true;

    public Server(int port, int capacity, int packetSize)
            throws SocketException, UnknownHostException {
        this.streamIN = new StreamIN(
                capacity, packetSize, InetAddress.getByName("localhost"), port);
        this.connectionStateMap = new HashMap<>();
    }

    public void start() {
        while(isRunning) {
            if(streamIN.getQueueSize() > 0) {
                System.out.println(streamIN.get().getValue());
            }
        }
    }

    public void stop() {
        isRunning = false;
    }
}
