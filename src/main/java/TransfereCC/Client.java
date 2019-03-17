package TransfereCC;

import AgenteUDP.StreamOUT;
import Estado.ConnectionState;
import Estado.State;

import java.net.SocketException;
import java.net.UnknownHostException;

public class Client {
    private ConnectionState connectionState;
    private StreamOUT streamOUT;

    public Client(ConnectionState connectionState)
            throws SocketException {
        this.connectionState = connectionState;
        this.streamOUT = new StreamOUT(
                150,
                connectionState.getDestiny(),
                connectionState.getDestinyPort());
    }

    public void start() {
        while(connectionState.getState() != State.FINISHED) {
            streamOUT.add("very useful data");
        }
    }
}
