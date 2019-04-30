package Test;

import Transport.GCVSocket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {



    public static void main( String[] args )  {

        try {

            GCVSocket cs = new GCVSocket(10000,true);

            cs.connect(args[0],7220);

            InputStream io = cs.receive();

            Scanner s = new Scanner(io).useDelimiter("\\A");

            while (true)
            {
                if(!s.hasNext()){
                    io = cs.receive();
                    s = new Scanner(io).useDelimiter("\\A");
                }

                //System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
                String tmp = s.hasNext() ? s.next() : " ";
            }

        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

}
