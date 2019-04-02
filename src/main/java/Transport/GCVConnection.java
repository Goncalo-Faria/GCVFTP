package Transport;

public final class GCVConnection {
    public static int port = 6969;
    public static int maxdata = 4000;/*65,507 bytes*/
    public static int maxcontrol = 2000; /*65,507 bytes*/


    public static int connection_request_capacity = 100;
    public static long connection_request_ttl= 100;
}
