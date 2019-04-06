package Transport;

public final class GCVConnection {
    public static int port = 6969;

    public static int stdmtu= 1472; /*508 bytes*/



    public static long connection_receive_ttl= 2000;

    public static int request_retry_number = 8;
    public static int request_retry_timeout = 6000;
}
