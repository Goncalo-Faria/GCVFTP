import AgenteUDP.Stream;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class GCVFTP {
    public static void main(String[] args) {
        try {
            Stream stream = new Stream(50, 50, "localhost", 8020);

            String hostname = "localhost";
            int port = 8020;

            InetAddress address = InetAddress.getByName(hostname);
            DatagramSocket socket = new DatagramSocket();

            while(true) {
                String string = "ola";
                byte[] buffer = string.getBytes();
                DatagramPacket request = new DatagramPacket(buffer, buffer.length, address, port);
                socket.send(request);

                String response = stream.get();
                if(response != null)
                    System.out.println(response);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
