package AgenteUDP;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class Stream implements Runnable {
    private DatagramSocket socket;
    private Queue<String> queue;
    private int packetSize;

    public Stream(int capacity, int packetSize, String ip, int port)
            throws SocketException, UnknownHostException {
        this.socket = new DatagramSocket(port, InetAddress.getByName(ip));
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.packetSize = packetSize;

        new Thread(this).start();
    }

    public String get() {
        return queue.poll();
    }

    public void run() {
        byte[] buf = new byte[packetSize];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        while(true) {
            try {
                socket.receive(packet);
                String quote = new String(buf, 0, packet.getLength());
                queue.add(quote);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
