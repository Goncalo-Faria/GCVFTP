package Test.GCV;

import Common.Connection;
import Transport.GCVConnection;
import Transport.GCVSocket;

import java.io.IOException;

public class Server {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("-");
        GCVSocket cs = new GCVSocket(GCVConnection.send_buffer_size,true, 7220);
        System.out.println("listening");
        cs = cs.listen();
        System.out.println("receiving");
        String s = Connection.receive(cs);
        long receivingTime = System.currentTimeMillis();
        String[] ss = s.split("-");
        long sentTime = Long.parseLong(ss[0]);
        System.out.println("Client -> " + (receivingTime - sentTime) + " ms");
    }
}
