package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;

public class StreamIN extends Stream {

    public StreamIN(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        super(capacity,packetSize,ip,port);
    }

    public DatagramPacket getDatagram() throws InterruptedException{
        return super.queue.take();
    }

    public byte[] get() throws InterruptedException{
        return queue.take().getData();
    }

    public void run() {
        Debugger.log("StreamIN is running");

        while(super.active()) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[packetSize], packetSize);
                socket.receive(packet);

                Debugger.log(
                        "Packet received from " +
                                packet.getAddress().toString() +
                                ", data: " + packet.getData().toString());

                queue.put(packet);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e){
                e.printStackTrace();
            }
        }

        socket.close();
    }
}
