package Model;

import java.security.PublicKey;

public class Customer {
    private String hostAddresss;
    private PublicKey publicKey;

    public Customer(String hostAddresss, PublicKey publicKey) {
        this.hostAddresss = hostAddresss;
        this.publicKey = publicKey;
    }

    public Customer(String hostAddresss) {
        this.hostAddresss = hostAddresss;
    }

    public String getHostAddresss() {
        return hostAddresss;
    }

    public void setHostAddresss(String hostAddresss) {
        this.hostAddresss = hostAddresss;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
