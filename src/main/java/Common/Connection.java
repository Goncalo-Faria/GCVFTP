package Common;

import Transport.GCVSocket;

import java.io.*;
import java.util.Scanner;

public class Connection {
    public static String receive(GCVSocket cs)  {
        StringBuilder stringBuilder = new StringBuilder();

        try {
            return new String( cs.receive() );


            /*
            System.out.println("::" + io.toString() +  "::");

            Scanner s = new Scanner(io).useDelimiter("\\A");


            while(s.hasNext()){
                String kk = s.next();
                stringBuilder.append(kk);
                System.out.println(":: "+ kk);
            }

            if( stringBuilder.toString().equals("") ){
                System.out.println(".");
                return receive(cs);
            }
            */
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static void send(GCVSocket cs, byte[] bytes) {
        try {

            Debugger.log("Start writing");
            //System.out.println(new String(bytes));

            OutputStream pout = cs.send();

            pout.write(bytes);
            pout.flush();
            pout.close();

            Debugger.log("############ Sent ################");
        } catch(IOException e){
            e.printStackTrace();
        }
    }
}
