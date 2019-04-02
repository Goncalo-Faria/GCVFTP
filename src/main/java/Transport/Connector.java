package Transport;

public interface Connector {
    Channel connect(String ip) throws InterruptedException;
}
