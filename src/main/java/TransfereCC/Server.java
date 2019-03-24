package TransfereCC;

import AgenteUDP.StreamIN;
import Estado.ConnectionState;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private StreamIN streamIN;
    private Map<Integer, ConnectionState> connectionStateMap;
    private boolean isRunning = true;

    public Server(int port, int capacity, int packetSize)
            throws SocketException, UnknownHostException {
        this.streamIN = new StreamIN(
                capacity, packetSize, InetAddress.getByName("localhost"), port);
        this.connectionStateMap = new HashMap<>();
    }

    public void start() {

        try {


            while (isRunning) {
            DatagramPacket packet = streamIN.get();
            System.out.println(new String(packet.getData()));
            }
        }catch(InterruptedException e){
            e.getStackTrace();
        }
    }


    public void stop() {
        isRunning = false;
    }
}
