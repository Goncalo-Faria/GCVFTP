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



    public static long rate_control_interval_sending = 100 ;
    public static long rate_control_interval_receiving = 100;

    public static int send_buffer_size =8192 ;
    public static int receive_buffer_size = 8192;
    public static int udp_send_buffer_size = send_buffer_size * stdmtu;
    public static int udp_receive_buffer_size = receive_buffer_size * stdmtu;

    public static int send_timeout = -1; // não dá timeout
    public static int receive_timeout = -1; // não dá timeout

}
