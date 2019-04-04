package AgenteUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

public class TransmissionChannel implements Channel {
    private final DatagramSocket cs;
    private final StationProperties in;
    private final StationProperties out;
    private final byte[] data_buffer ;

    public TransmissionChannel(StationProperties in,
                               StationProperties out) throws SocketException {
        this.cs = new DatagramSocket(in.port());
        this.cs.connect(out.ip(),out.port());
        this.in = in;
        this.out = out;
        this.data_buffer = new byte[in.packetsize()];
    }

    public TransmissionChannel(DatagramSocket cs,
                               StationProperties in,
                               StationProperties out) throws SocketException {
        this.cs = cs;
        this.cs.connect(out.ip(),out.port());
        this.in=in;
        this.out = out;
        this.data_buffer = new byte[in.packetsize()];
    }

    public void send(byte[] data) throws IOException {
        int sz = data.length;

        if(data.length > out.packetsize())
            sz = out.packetsize();

        cs.send(new DatagramPacket(data, 0, sz, out.ip(), out.port()));
    }

    public byte[] receive() throws IOException{
        DatagramPacket packet = new DatagramPacket(this.data_buffer,in.packetsize());

        cs.receive(packet);
        byte[] dest = new byte[packet.getLength()];
        ByteBuffer.wrap(packet.getData()).get(new byte[packet.getLength()],0,packet.getLength());

        return dest;
    }

}
