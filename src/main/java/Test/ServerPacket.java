package Test;

import Transport.GCVConnection;
import Transport.GCVSocket;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.TimeoutException;


public class ServerPacket {
    private static boolean isRunning = true;


    public static void main( String[] args ) {
        String message = "very useful data, so they say";
        try {
            if( args.length > 0) {
                if (args[1].equals("debug"))
                    Debugger.setEnabled(true);
                else
                    Debugger.setEnabled(false);
            }else{
                Debugger.setEnabled(false);
            }

            GCVSocket cs = new GCVSocket(12000,true, 6969);

            cs.connect(args[0]);

            int i = 0;
            while ( ++i < 200000 ) {

                cs.send((message + " " + i + "\n").getBytes());
                //System.out.println("::::: i'm sending shit " + i );
            }

            while(true){
                Thread.sleep(1000);
            }

        }catch(IOException|InterruptedException|TimeoutException e){
            e.printStackTrace();
        }
    }

    public static void stop() {
        isRunning = false;
    }
}
