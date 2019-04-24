package Test;

import Transport.Socket;
import Transport.Start.GCVConnector;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {

    private static GCVConnector connect = new GCVConnector(7220,200,true);


    public static void main( String[] args )  {

        try {
            Socket cs = connect.bind(InetAddress.getLocalHost());

            InputStream io = cs.receive();

            Scanner s = new Scanner(io).useDelimiter("\\A");

            while (true)
            {
                if(!s.hasNext()){
                    io = cs.receive();
                    s = new Scanner(io).useDelimiter("\\A");
                }

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.print(s.hasNext() ? s.next() : "");
                Thread.sleep(1000);
            }

        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

}
