package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;

public class StreamOUT extends Stream {

    private final StationProperties st;

    public StreamOUT(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        super(capacity, packetSize, ip, port);
        this.st = new StationProperties(ip, capacity, port, StationProperties.ConnectionType.SEND, packetSize);

    }

    public StreamOUT(StationProperties st)
            throws SocketException {
        super(st);
        this.st = st;
    }

    public void add(byte[] data) throws InterruptedException{

        int sz = data.length;

        if(data.length > this.maxpacketSize)
            sz = this.maxpacketSize;

        queue.put(new DatagramPacket(data, 0,sz, st.ip(), st.port()));
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
                        Debugger.log("Packet sent to " + st.ip() + ":" + st.port());
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
