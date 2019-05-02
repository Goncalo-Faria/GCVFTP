package Test;

import Transport.GCVConnection;
import Transport.GCVSocket;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Client {



    public static void main( String[] args )  {

        try {
            if( args.length > 1 ) {
                if (args[1].equals("debug"))
                    Debugger.setEnabled(true);
                else
                    Debugger.setEnabled(false);
            }else{
                Debugger.setEnabled(false);
            }


            GCVSocket cs = new GCVSocket(GCVConnection.send_buffer_size,true);

            cs.connect(args[0],7220);

            InputStream io = cs.receive();

            Scanner s = new Scanner(io).useDelimiter("\\A");

            while (true)
            {
                if(!s.hasNext()){
                    io = cs.receive();
                    s = new Scanner(io).useDelimiter("\\A");
                }

                String tmp = s.hasNext() ? s.next() : " ";
            }

        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

}
