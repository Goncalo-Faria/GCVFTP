package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class StreamIN implements Runnable {
    private DatagramSocket socket;
    private ArrayBlockingQueue<DatagramPacket> queue;
    private int packetSize;
    private AtomicBoolean isRunning;

    public StreamIN(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        this.socket = new DatagramSocket(port, ip);
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.packetSize = packetSize;
        this.isRunning= new AtomicBoolean(true);

        new Thread(this).start();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int remainingCapacity(){
        return this.queue.remainingCapacity();
    }

    public DatagramPacket getDatagram() throws InterruptedException{
        return queue.take();
    }

    public byte[] get() throws InterruptedException{
        return queue.take().getData();
    }

    public void stop() {
        isRunning.set(false);
    }

    public void run() {
        Debugger.log("StreamIN is running");

        while(isRunning.get()) {
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
