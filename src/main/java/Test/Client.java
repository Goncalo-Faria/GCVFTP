package Test;

import Transport.Socket;
import Transport.Start.GCVConnector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeoutException;

public class Client {

    private static GCVConnector connect = new GCVConnector(7220,200);


    public static void main( String[] args )  {
        String message = "very useful data, so they say";

        try {
            Socket cs = connect.bind(InetAddress.getLocalHost());
            int i = 0;
            while ( ++i < 2000 ) {
                cs.send((message + " " + i).getBytes());
                Thread.sleep(100);
                //System.out.println("::::: i'm sending shit " + i );
            }

        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

}
