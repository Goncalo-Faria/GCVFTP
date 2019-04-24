package Test;

import Transport.Socket;
import Transport.Start.GCVListener;
import Transport.Start.Listener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Scanner;


public class Server {
    private static boolean isRunning = true;
    private static Listener listener = new GCVListener();

    public static void main( String[] args ) {
        String message = "very useful data, so they say";

        try {

            Socket cs = listener.accept();

            int i = 0;
            while ( ++i < 10 ) {
                cs.send((message + " " + i + "\n").getBytes());
                //System.out.println("::::: i'm sending shit " + i );
            }

            while(true){
                Thread.sleep(1000);
            }

        }catch(IOException|InterruptedException e){
            e.printStackTrace();
        }
    }

    public static void stop() {
        isRunning = false;
    }
}
