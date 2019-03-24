package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamOUT implements Runnable {
    private DatagramSocket socket;
    private ArrayBlockingQueue<DatagramPacket> queue;
    private int port;
    private InetAddress inetAddress;
    private AtomicBoolean isRunning;
    private int packetSize;

    public StreamOUT(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        this.socket = new DatagramSocket();
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.port = port;
        this.inetAddress = ip;
        this.isRunning = new AtomicBoolean(true);
        this.packetSize = packetSize;

        new Thread(this).start();
    }

    public void add(byte[] data) throws InterruptedException{

        int sz = data.length;

        if(data.length > this.packetSize)
            sz = this.packetSize;

        queue.put(new DatagramPacket(data, 0,sz, this.inetAddress, this.port));
    }

    public void stop() {
        isRunning.set(false);
    }

    public void run() {

        while(isRunning.get()) {
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
