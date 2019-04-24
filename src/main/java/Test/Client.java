package Test;

import Transport.Socket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {



    public static void main( String[] args )  {

        try {

            Socket cs = new Socket(200,true);

            cs.connect(InetAddress.getLocalHost(),7220);

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
            }

        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

}
