package Transport;

public interface Channel {
    void send( byte[] data) throws InterruptedException;
    byte[] receive() throws InterruptedException;
}
