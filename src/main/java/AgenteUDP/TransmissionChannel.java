package AgenteUDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;

import static Transport.GCVConnection.udp_receive_buffer_size;

public class TransmissionChannel implements Channel {
    private final DatagramSocket cs;
    private final StationProperties in;
    private final StationProperties out;
    private final byte[] data_buffer ;

    public TransmissionChannel(StationProperties send,
                               StationProperties receive) throws SocketException {
        this.cs = new DatagramSocket(send.port());
        this.cs.setSendBufferSize( send.channel_buffer_size() );
        this.cs.setReceiveBufferSize( receive.channel_buffer_size() );
        this.cs.connect(receive.ip(),receive.port());
        this.in = send;
        this.out = receive;
        this.data_buffer = new byte[in.packetsize()];
    }

    public TransmissionChannel(DatagramSocket cs,
                               StationProperties send,
                               StationProperties receive) throws SocketException {
        this.cs = cs;
        this.cs.setSendBufferSize( send.channel_buffer_size() );
        this.cs.setReceiveBufferSize( receive.channel_buffer_size() );
        this.cs.connect(receive.ip(),receive.port());
        this.in = send;
        this.out = receive;
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

        //System.out.println(new String(this.data_buffer));

        cs.receive(packet);
        byte[] dest = new byte[packet.getLength()];
        ByteBuffer.wrap(packet.getData()).get(dest,0,packet.getLength());

        return dest;
    }

    public void close(){
        this.cs.disconnect();
        this.cs.close();
    }

    public StationProperties getinStationProperties(){
        return in;
    }

    public StationProperties getoutStationProperties(){
        return out;
    }

}
