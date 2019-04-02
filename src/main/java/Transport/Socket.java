package Transport;

import AgenteUDP.Channel;
import Transport.Unit.DataPacket;

import java.util.concurrent.PriorityBlockingQueue;

public class Socket implements Channel{

    //int msb = (m & 0xff) >> 7;

    private PriorityBlockingQueue<DataPacket> bag;

    /* receiver
     * manda ack
     * fica à escuta de dados
     * espera ack2
     * */

    /* sender
     * manda ack2
     * manda dados
     * */

    public Socket( StationProperties me, StationProperties caller){
        this.bag  = new PriorityBlockingQueue<>();

    }

    public void send( byte[] data) throws InterruptedException{

    }

    public byte[] receive() throws InterruptedException{
        return null;
    }
}
