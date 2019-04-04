package Transport.Start;


import Transport.Socket;
import java.io.IOException;


public interface Listener {
    Socket accept() throws InterruptedException, IOException ;
}