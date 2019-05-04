package Model;

import java.net.InetAddress;

public class Peer {
    private InetAddress inetAddress;

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
}
