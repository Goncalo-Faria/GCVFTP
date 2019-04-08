package Transport;

public final class GCVConnection {
    public static int port = 6969;
    public static int stdmtu= 1460; /* 496 bytes*/
    public static long rate_control_interval = 100;
    public static int tau = 9;
    public static float decrease_factor = 0.5F;
    public static long connection_receive_ttl= 2000;
    public static int request_retry_number = 8;
    public static int request_retry_timeout = 2000;
}
