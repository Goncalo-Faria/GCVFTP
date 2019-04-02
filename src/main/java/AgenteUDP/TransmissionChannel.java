package AgenteUDP;
import Transport.StationProperties;

import java.net.SocketException;

public class TransmissionChannel implements Channel {
    private StreamIN in;
    private StreamOUT out;

    public TransmissionChannel(int mtu, StationProperties in, StationProperties out) throws SocketException {
        this.in = new StreamIN(in.capacity(), mtu, in.ip(), in.port());
        this.out = new StreamOUT(out.capacity(), mtu, out.ip(), out.port());
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
