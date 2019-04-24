package Transport;

public final class GCVConnection {
    public static final int port = 6969;
    public static final int stdmtu= 1460; /* 496 bytes*/

    public static final long rate_control_interval = 100;

    public static final int tau = 9;
    public static final float decrease_factor = 0.5F;
    public static final long connection_receive_ttl= 2000;
    public static final int request_retry_number = 8;
    public static final int request_retry_timeout = 2000;

    public static final float rrt_factor = 0.125F;
    public static final float var_rrt_factor = 0.25F;


    public static final long rate_control_interval_sending = 100 ;
    public static final long rate_control_interval_receiving = 100 ;

    public static final int send_buffer_size = 8192 ;
    public static final int receive_buffer_size = 8192;
    public static final int udp_send_buffer_size = send_buffer_size * stdmtu;
    public static final int udp_receive_buffer_size = receive_buffer_size * stdmtu;

    public static final int send_timeout = -1; // não dá timeout
    public static final int receive_timeout = -1; // não dá timeout

    public static final int initial_window_size = 2;

}
