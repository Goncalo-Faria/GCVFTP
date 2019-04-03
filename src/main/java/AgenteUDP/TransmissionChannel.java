package AgenteUDP;

import java.net.SocketException;

public class TransmissionChannel implements Channel {
    private StreamIN in;
    private StreamOUT out;

    public TransmissionChannel(StationProperties in, StationProperties out) throws SocketException {
        this(new StreamIN(in.capacity(), in.packetsize(), in.ip(), in.port()), out);
    }

    public TransmissionChannel(StreamIN in, StationProperties out) throws SocketException {
        this.in = in;
        this.out = new StreamOUT(out.capacity(), out.packetsize(), out.ip(), out.port());
    }

    public void send(byte[] data) throws InterruptedException{

        this.out.add(data);
    }

    public byte[] receive() throws InterruptedException{
        return this.in.get();
    }

    public int sendWindow(){
        return out.window();
    }

    public int receiveWindow(){
        return in.window();
    }

}
