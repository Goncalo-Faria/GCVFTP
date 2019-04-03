package Transport.Start;

import AgenteUDP.StreamIN;
import AgenteUDP.StreamOUT;
import Transport.GCVConnection;
import Transport.Socket;
import AgenteUDP.StationProperties;
import Transport.Unit.ControlPacket;
import Transport.Unit.Packet;


import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GCVConnector extends TimerTask implements Connector {

    private StreamOUT outs;
    private StreamIN ins;
    private ControlPacket connection_message;
    private AtomicBoolean active = new AtomicBoolean(true);
    private AtomicInteger count = new AtomicInteger(0);
    private Thread cur_t;

    private int outCapacity;
    private StationProperties in_properties;
    private StationProperties out_properties;

    public GCVConnector(int my_port,int inCapacity, int outCapacity){
        try {
            this.in_properties = new StationProperties(
                    InetAddress.getByName("localhost"),
                    inCapacity, my_port,
                    StationProperties.ConnectionType.RECEIVE,GCVConnection.maxcontrol);

            this.connection_message = new ControlPacket(
                    ByteBuffer.allocate(4).putInt(this.in_properties.port()).put(this.in_properties.ip().getAddress()).array(),
                    ControlPacket.Type.HI,
                    0);

            this.outCapacity = outCapacity;
            this.cur_t = Thread.currentThread();
        }catch(UnknownHostException e){
            e.getStackTrace();
        }

    }

    public Socket bind(String ip) throws InterruptedException, UnknownHostException, SocketException, TimeoutException {

        this.active.set(true);
        this.outs = new StreamOUT(
                GCVConnection.request_retry_number,
                8 + ControlPacket.header_size + 4 + 8,/* udp header + our header + port + ip */
                InetAddress.getByName(ip),
                GCVConnection.port);

        this.ins = new StreamIN(this.in_properties);

        try{
            while(this.active.get()) {
                Packet du = Packet.parse(this.ins.get());
                if(du instanceof ControlPacket){
                    ControlPacket cdu = (ControlPacket)du;
                    if( cdu.getType().equals(ControlPacket.Type.SUP) ){
                        cdu.startBuffer();
                        int port = cdu.getInt();
                        this.active.set(false);
                        this.outs.stop();
                        this.out_properties = new StationProperties(
                                InetAddress.getByName(ip),
                                this.outCapacity,
                                port,
                                StationProperties.ConnectionType.SEND,
                                GCVConnection.maxdata);
                    }
                }
            }

        }catch(InterruptedException e){
           if(this.active.get()){
               e.getStackTrace();
           }else{
               throw new TimeoutException();
           }
        }

        return new Socket(this.ins,this.out_properties);

    }

    public void run(){
        try {
            this.outs.add(connection_message.serialize());
        }catch (InterruptedException e){
            e.getStackTrace();
        }

        if(count.get() >= GCVConnection.request_retry_number){
            active.set(false);
            this.outs.stop();
            this.cur_t.interrupt();
        }

    }
}
