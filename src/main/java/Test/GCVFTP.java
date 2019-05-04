package Test;

import Common.GlobalVariables;
import Transport.GCVConnection;
import Transport.GCVSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GCVFTP {
    private static Client client;

    public static void main(String[] args) {
        if(args.length > 0) {
            GlobalVariables.filesPath = args[0];
        }

        try {
            GCVSocket cs = new GCVSocket(GCVConnection.send_buffer_size,true);
            client = new Client(cs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String input;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("> ");

            try {
                input = br.readLine();

                if(input.equals("exit")) {
                    System.exit(0);
                } else {
                    parse(input);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void parse(String input) {
        List<String> s = new ArrayList<>(Arrays.asList(input.split(" ")));

        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getByName(s.get(1));
        } catch (UnknownHostException | IndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }

        switch (s.get(0)) {
            case "put":
                s.remove(0);
                s.remove(0);
                client.put(inetAddress, s);
                break;
            case "get":
                s.remove(0);
                s.remove(0);
                client.get(inetAddress, s);
                break;
            case "register":
                client.register(inetAddress);
            default:
                System.out.println("Unknown command");
        }
    }
}
