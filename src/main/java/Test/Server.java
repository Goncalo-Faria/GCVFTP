package TransfereCC;

import Transport.Socket;
import Transport.Start.GCVListener;
import Transport.Start.Listener;

import java.io.IOException;


public class Server {
    private static boolean isRunning = true;
    private static Listener listener = new GCVListener();

    public static void main( String[] args ) {

        try {

            Socket cs = listener.accept();

            while (isRunning)
            {
                byte[] data = cs.receive();
                System.out.println("says: " + new String(data));
            }

        }catch(IOException|InterruptedException e){
            e.printStackTrace();
        }
    }


    public static void stop() {
        isRunning = false;
    }
}
