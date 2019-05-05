package Test;

import Transport.GCVConnection;
import Transport.GCVSocket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.channels.Pipe;
import java.util.Scanner;


public class Server {
    private static boolean isRunning = true;


    public static void main( String[] args ) {
        String message = "very useful data, so they say";
        try {
            if( args.length > 0) {
                if (args[0].equals("debug"))
                    Debugger.setEnabled(true);
                else
                    Debugger.setEnabled(false);
            }else{
                Debugger.setEnabled(false);
            }

            GCVSocket cs = new GCVSocket(GCVConnection.send_buffer_size,true,6969);

            cs = cs.listen();

            PipedInputStream pin = new PipedInputStream(10000);
            PipedOutputStream pout = new PipedOutputStream(pin);

            int i = 0;

            Debugger.log("Start writing");

            cs.send(pin);

            while ( ++i < 200000 )
                pout.write((message + " " + i + "\n").getBytes());

            pout.flush();

            pout.close();

            Debugger.log("############ Sent ################");

            while(true)
                Thread.sleep(1000);


        }catch(IOException|InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void stop() {
        isRunning = false;
    }
}
