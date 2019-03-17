package Estado;

import AgenteUDP.StreamIN;
import AgenteUDP.StreamOUT;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class ConnectionState {
    private static int nextID = 0;

    private int id;
    private ConnectionType connectionType;
    private String file;
    private InetAddress origin;
    private int originPort;
    private InetAddress destiny;
    private int destinyPort;
    private List<StreamOUT> streamOUTList;
    private List<StreamIN> streamINList;
    private State state;

    public ConnectionState(
            ConnectionType connectionType, String file, InetAddress origin,
            int originPort, InetAddress destiny, int destinyPort) {
        this.id = ConnectionState.nextID++;
        this.file = file;
        this.origin = origin;
        this.originPort = originPort;
        this.destiny = destiny;
        this.destinyPort = destinyPort;

        this.streamOUTList = new ArrayList<>();
        this.streamINList = new ArrayList<>();
        this.state = State.INITIATED;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFile() {
        return file;
    }

    public InetAddress getOrigin() {
        return origin;
    }

    public int getOriginPort() {
        return originPort;
    }

    public InetAddress getDestiny() {
        return destiny;
    }

    public int getDestinyPort() {
        return destinyPort;
    }

    public List<StreamOUT> getStreamOUTList() {
        return streamOUTList;
    }

    public void addToStreamOUT(StreamOUT streamOUT) {
        streamOUTList.add(streamOUT);
    }

    public boolean removeFromStreamOUT(StreamOUT streamOUT) {
        return streamOUTList.remove(streamOUT);
    }

    public List<StreamIN> getStreamINList() {
        return streamINList;
    }

    public void addToStreamIN(StreamIN streamIN) {
        streamINList.add(streamIN);
    }

    public boolean removeFromStreamIN(StreamIN streamIN) {
        return streamINList.remove(streamIN);
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }
}
