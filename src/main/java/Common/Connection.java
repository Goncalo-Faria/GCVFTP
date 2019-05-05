package Common;

import Transport.GCVSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Scanner;

public class Connection {
    public static String receive(GCVSocket cs)  {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream io = cs.receive();

            Scanner s = new Scanner(io).useDelimiter("\\A");

            byte[] buffer = new byte[40000];
            int i = 0;
            while (i  < 1)
            {
                i++;
                if(!s.hasNext()){
                    io = cs.receive();
                    int val = io.read(buffer,0,buffer.length);
                    s = new Scanner(io).useDelimiter("\\A");
                }

                String tmp = s.hasNext() ? s.next() : "";
                stringBuilder.append(tmp);
            }
            io.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }

    public static void send(GCVSocket cs, byte[] bytes) {
        try {
            PipedInputStream pin = new PipedInputStream(10000);
            PipedOutputStream pout = new PipedOutputStream(pin);

            Debugger.log("Start writing");

            cs.send(pin);
            pout.write(bytes);
            pout.flush();
            pout.close();

            Debugger.log("############ Sent ################");
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
