package AgenteUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class TransmissionChannel implements Channel {
    private DatagramSocket cs;
    private StationProperties in;
    private StationProperties out;

    public TransmissionChannel(StationProperties in,
                               StationProperties out) throws SocketException {
        this.cs = new DatagramSocket(in.port());
        this.cs.connect(out.ip(),out.port());
        this.in = in;
        this.out = out;

    }

    public TransmissionChannel(DatagramSocket cs,
                               StationProperties in,
                               StationProperties out) throws SocketException {
        this.cs = cs;
        this.cs.connect(out.ip(),out.port());
        this.out = out;
    }

    public void send(byte[] data) throws IOException {
        int sz = data.length;

        if(data.length > out.packetsize())
            sz = out.packetsize();

        System.out.println("hey ::" + out.port() + "::" +  out.ip() );

        cs.send(new DatagramPacket(data, 0, sz, out.ip(), out.port()));
    }

    public byte[] receive() throws IOException{
        DatagramPacket packet = new DatagramPacket(new byte[in.packetsize()],in.packetsize());

        cs.receive(packet);

        return packet.getData();
    }

}
