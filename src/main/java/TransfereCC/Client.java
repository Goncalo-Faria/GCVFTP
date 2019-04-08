package TransfereCC;

import Transport.Socket;
import Transport.Start.GCVConnector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

public class Client {

    private static GCVConnector connect = new GCVConnector(7220,1000,20,10);
    private static boolean isRunning = true;

    public static void main( String[] args )  {
        String message = "very useful data, so they say";

        try {
            Socket cs = connect.bind(InetAddress.getLocalHost());
            while (isRunning) {
                cs.send(message.getBytes());
                Thread.sleep(2000);
            }
        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void stop() {
        isRunning = false;
    }
}
