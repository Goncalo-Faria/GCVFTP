package Transport;

import AgenteUDP.StreamOUT;
import Transport.Channel;
import Transport.DataPacket;

import java.util.concurrent.PriorityBlockingQueue;

public class Socket implements Channel{

    //int msb = (m & 0xff) >> 7;

    private PriorityBlockingQueue<DataPacket> bag;
    private Channel cs;

    public Socket( StationProperties in, StreamOUT s){
        this.cs = cs;
        this.bag  = new PriorityBlockingQueue<>();

        //            // esperar ack
        //            // mandar ack2
    }

    void send( byte[] data) throws InterruptedException{

    }

    byte[] receive() throws InterruptedException{

    }
}
