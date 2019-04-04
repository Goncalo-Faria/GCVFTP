package Transport;

public final class GCVConnection {
    public static int port = 6969;
    public static int maxdata = 4000;/*65,507 bytes*/
    public static int maxcontrol = 2000; /*65,507 bytes*/



    public static long connection_receive_ttl= 2000;

    public static int request_retry_number = 8;
    public static int request_retry_timeout = 6000;
}
