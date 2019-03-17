import Estado.ConnectionState;
import Estado.ConnectionType;
import TransfereCC.Client;
import TransfereCC.Server;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class GCVFTP {
    public static void main(String[] args)
            throws UnknownHostException, SocketException {

        if(args[0].equals("GET") || args[0].equals("PUT")) {                    // 'PUT' or 'GET' - args[0]
            ConnectionState connectionState = new ConnectionState(
                    args[0].equals("GET") ?
                            ConnectionType.SEND :
                            ConnectionType.RECEIVE,
                    args[1],                                                    // FILENAME - args[1]
                    InetAddress.getByName("localhost"),
                    8000,
                    InetAddress.getByName(args[2]),                             // DESTINY ADDRESS - args[2]
                    Integer.parseInt(args[3])                                   // DESTINY PORT - args[3]
            );
            Client client = new Client(connectionState);
            client.start();
        }
        else {
            Server server = new Server(
                    Integer.parseInt(args[0]),                                  //PORT - args[0]
                    Integer.parseInt(args[1]),                                  //CAPACITY - args[1]
                    Integer.parseInt(args[2])                                   //PACKET SIZE - args[2]
            );
            server.start();
        }
    }
}
