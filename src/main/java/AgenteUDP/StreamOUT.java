package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;

public class StreamOUT extends Stream {

    private final int port;
    private final InetAddress inetAddress;

    public StreamOUT(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        super(capacity, packetSize, ip, port);
        this.inetAddress = ip;
        this.port = port;
    }

    public void add(byte[] data) throws InterruptedException{

        int sz = data.length;

        if(data.length > this.maxpacketSize)
            sz = this.maxpacketSize;

        queue.put(new DatagramPacket(data, 0,sz, this.inetAddress, this.port));
    }

    public int window(){
        return this.capacity -  this.queue.remainingCapacity();
    }


    public void run() {

        while(this.isActive()) {
            try {
                if (queue.size() > 0) {
                    DatagramPacket packet = queue.take();

                    try {
                        socket.send(packet);
                        Debugger.log("Packet sent to " + inetAddress + ":" + port);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }catch(InterruptedException e){
                e.getStackTrace();
            }
        }

        socket.close();
    }
}
