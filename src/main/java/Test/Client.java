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

            GCVSocket cs = new GCVSocket(GCVConnection.send_buffer_size,true,8855);

            cs.connect(args[0]);

            InputStream io = cs.receive();

            Scanner s = new Scanner(io).useDelimiter("\\A");

            byte[] buffer = new byte[40000];
            int i = 0;
            while (true )
            {
                i++;
                //System.out.println(i);
                if(!s.hasNext()){
                    io = cs.receive();
                    //int val = io.read(buffer,0,buffer.length);
                    s = new Scanner(io).useDelimiter("\\A");
                    //Debugger.log("##########################");

                    //System.out.println("hey");
                    //System.out.println( new String(buffer) );

                }

                String tmp = s.hasNext() ? s.next() : "";

                System.out.println( tmp );

            }

            //cs.close();

        } catch(IOException | TimeoutException| InterruptedException e){
            e.printStackTrace();
        }
    }

}
