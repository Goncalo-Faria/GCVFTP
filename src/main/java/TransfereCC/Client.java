package TransfereCC;

import Transport.Socket;
import Transport.Start.GCVConnector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

public class Client {

    private static GCVConnector connect = new GCVConnector(7220);
    private static boolean isRunning = true;

    public static void main( String[] args )  {
        String message = "very useful data";

        try {
            Socket cs = connect.bind(InetAddress.getLocalHost());
            while (isRunning) {
                cs.send(message.getBytes());
            }
        } catch(IOException | TimeoutException e){
            e.printStackTrace();
        }
    }

    public static void stop() {
        isRunning = false;
    }
}
