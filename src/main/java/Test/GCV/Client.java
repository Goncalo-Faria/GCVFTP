package Test.GCV;

import Common.Connection;
import Transport.GCVConnection;
import Transport.GCVSocket;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Client {
    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        System.out.println("-");
        GCVSocket cs = new GCVSocket(GCVConnection.send_buffer_size,true,6969);
        System.out.println("connecting");
        cs.connect(args[0], 7220);
        System.out.println("building");
        StringBuilder sb = new StringBuilder();
        sb.append(System.currentTimeMillis());
        sb.append("-");
        for(int i = 0; i < 16777216; i++) {
            sb.append("aaaaaaaa");
        }
        cs.send(sb.toString().getBytes());
        System.out.println("sent");
    }
}
