package AgenteUDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Stream implements Runnable{

    final DatagramSocket socket;
    ArrayBlockingQueue<DatagramPacket> queue;
    final int packetSize;
    private AtomicBoolean isRunning;
    final int capacity;

    public Stream(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        this.socket = new DatagramSocket(port, ip);
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.packetSize = packetSize;
        this.isRunning= new AtomicBoolean(true);
        this.capacity = capacity;
        new Thread(this).start();
    }

    public int window(){
        return this.capacity -  this.queue.remainingCapacity();
    }

    public void stop() {
        isRunning.set(false);
    }

    public abstract void run();

    public boolean active(){
        return isRunning.get();
    }
}
