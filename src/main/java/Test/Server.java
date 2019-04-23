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

        try {

            Socket cs = listener.accept();

            InputStream io = cs.receive();

            Scanner s = new Scanner(io).useDelimiter("\\A");


            while (isRunning)
            {
                if(!s.hasNext()){
                    io = cs.receive();
                    s = new Scanner(io).useDelimiter("\\A");
                }

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>");
                System.out.print(s.hasNext() ? s.next() : "");
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
