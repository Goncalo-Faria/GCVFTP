package Test;

import Transport.GCVSocket;

import java.io.IOException;


public class Server {
    private static boolean isRunning = true;


    public static void main( String[] args ) {
        String message = "very useful data, so they say";
        try {
            if( args.length > 1 ) {
                if (args[1].equals("debug"))
                    Debugger.setEnabled(true);
                else
                    Debugger.setEnabled(false);
            }else{
                Debugger.setEnabled(false);
            }

            GCVSocket cs = new GCVSocket(10000,true);

            cs.listen();

            int i = 0;
            while ( ++i < 200000 ) {
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
