package Model;

import java.io.Serializable;
import java.security.PublicKey;

public class Packet implements Serializable {
    private ConnectionType connectionType;
    private TFiles tFiles;
    private PublicKey publicKey;
    private String hostAddress;

    public Packet(ConnectionType connectionType, TFiles tFiles, PublicKey publicKey, String hostAddress) {
        this.connectionType = connectionType;
        this.tFiles = tFiles;
        this.publicKey = publicKey;
        this.hostAddress = hostAddress;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(ConnectionType connectionType) {
        this.connectionType = connectionType;
    }

    public TFiles gettFiles() {
        return tFiles;
    }

    public void settFiles(TFiles tFiles) {
        this.tFiles = tFiles;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void setHostAddress(String hostAddress) {
        this.hostAddress = hostAddress;
    }
}
