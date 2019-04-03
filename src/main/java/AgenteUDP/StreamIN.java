package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;

public class StreamIN extends Stream {

    public StreamIN(int capacity, int maxpacketSize, InetAddress ip, int port)
            throws SocketException {
        super(capacity,maxpacketSize,ip,port);
    }

    public StreamIN(StationProperties st)
            throws SocketException {
        super(st);
    }

    public DatagramPacket getDatagram() throws InterruptedException{
        return super.queue.take();
    }

    public byte[] get() throws InterruptedException{
        return queue.take().getData();
    }

    public void run() {
        Debugger.log("StreamIN is running");

        while(super.isActive()) {
            try {
                DatagramPacket packet = new DatagramPacket(new byte[maxpacketSize], maxpacketSize);
                socket.receive(packet);

                Debugger.log(
                        "Packet received from " +
                                packet.getAddress().toString() +
                                ", data: " + packet.getData().toString());

                this.queue.put(packet);

            } catch (IOException|InterruptedException e) {
                e.printStackTrace();
            }
        }

        socket.close();
    }
}
