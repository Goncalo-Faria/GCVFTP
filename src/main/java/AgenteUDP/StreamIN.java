package AgenteUDP;

import helper.Debugger;
import javafx.util.Pair;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class StreamIN implements Runnable {
    private DatagramSocket socket;
    private Queue<Pair<DatagramPacket, String>> queue;
    private int packetSize;
    private boolean isRunning;

    public StreamIN(int capacity, int packetSize, InetAddress ip, int port)
            throws SocketException {
        this.socket = new DatagramSocket(port, ip);
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.packetSize = packetSize;
        this.isRunning = true;

        new Thread(this).start();
    }

    public int getQueueSize() {
        return queue.size();
    }

    public Pair<DatagramPacket, String> get() {
        return queue.poll();
    }

    public void stop() {
        isRunning = false;
    }

    public void run() {
        Debugger.log("StreamIN is running");

        byte[] buf = new byte[packetSize];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while(isRunning) {
            try {
                socket.receive(packet);
                String data = new String(buf, 0, packet.getLength());

                Debugger.log(
                        "Packet received from " +
                                packet.getAddress().toString() +
                                ", data: " + data);

                queue.add(new Pair<>(packet, data));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        socket.close();
    }
}
