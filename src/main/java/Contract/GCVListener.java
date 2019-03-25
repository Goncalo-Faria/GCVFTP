package Contract;

public interface GCVListener {
    Channel accept(int port) throws InterruptedException;
}
