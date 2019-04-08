package TransfereCC;

import Transport.Socket;
import Transport.Start.GCVConnector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

public class Client {

    private static GCVConnector connect = new GCVConnector(7220,1000,20,1000);
    private static boolean isRunning = true;

    public static void main( String[] args )  {
        String message = "very useful data, so they say";

        try {
            Socket cs = connect.bind(InetAddress.getLocalHost());
            int i = 0;
            while (isRunning && ++i < 15 ) {
                cs.send(message.getBytes());
                Thread.sleep(500);
            }
            cs.close((short)69);
        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void stop() {
        isRunning = false;
    }
}
