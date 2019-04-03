package TransfereCC;

import AgenteUDP.Channel;
import AgenteUDP.StreamIN;
import Estado.ConnectionState;
import Transport.Socket;
import Transport.Start.GCVListener;
import Transport.Start.Listener;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Server {
    private boolean isRunning = true;
    private Listener listener = new GCVListener(1,1);

    public Server(){

    }

    public void start() {

        try {
            Channel cs = listener.accept();

            while (isRunning)
            {
                 byte[] data = cs.receive();
                System.out.println(new String(data));
            }

        }catch(InterruptedException|SocketException e){
            e.getStackTrace();
        }
    }


    public void stop() {
        isRunning = false;
    }
}
