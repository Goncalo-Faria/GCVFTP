package AgenteUDP;

import helper.Debugger;

import java.io.IOException;
import java.net.*;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class StreamOUT implements Runnable {
    private DatagramSocket socket;
    private Queue<String> queue;
    private int port;
    private InetAddress inetAddress;
    private boolean isRunning;

    public StreamOUT(int capacity, InetAddress ip, int port)
            throws SocketException {
        this.socket = new DatagramSocket();
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.port = port;
        this.inetAddress = ip;
        this.isRunning = true;

        new Thread(this).start();
    }

    public void add(String packet) {
        queue.add(packet);
    }

    public void stop() {
        isRunning = false;
    }

    public void run() {
        while(isRunning) {
            if (queue.size() > 0) {
                byte[] buf = queue.poll().getBytes();
                DatagramPacket packet = new DatagramPacket(
                        buf, buf.length, inetAddress, port);

                try {
                    socket.send(packet);
                    Debugger.log("Packet sent to " + inetAddress +":"+ port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        socket.close();
    }
}
