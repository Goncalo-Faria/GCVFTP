package Test;

import Transport.GCVSocket;

import java.io.IOException;


public class Server {
    private static boolean isRunning = true;


    public static void main( String[] args ) {
        String message = "very useful data, so they say";

        try {

            GCVSocket cs = new GCVSocket();

            cs.listen();

            int i = 0;
            while ( ++i < 80 ) {
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
