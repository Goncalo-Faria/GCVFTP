package Test;

import Common.BytesConverter;
import Common.GlobalVariables;
import Common.RSAKeys;
import Model.*;
import Transport.GCVSocket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.PublicKey;
import java.util.*;
import java.util.concurrent.TimeoutException;

public class Server implements Runnable {
    private GCVSocket cs;

    // LIST OF OTHER SERVERS
    private Map<Integer, Peer> peers;

    // REGISTERED CLIENTS
    private Map<PublicKey, Customer> customers;

    // FILES I KNOW - i can have or i can know where they exist
    private Map<String, TFile> files;

    // COMMUNICATION WAITING TO BE SENT
    private Queue<Communication> communications;

    // MY ADDRESS
    private String address;

    Server() {
        try {
            this.cs = new GCVSocket(10000, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.peers = new HashMap<>();
        this.customers = new HashMap<>();
        this.communications = new PriorityQueue<>();
        this.files = new HashMap<>();

        try {
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            if(communications.isEmpty()) {
                // IF I DON'T HAVE MORE COMMUNICATION IN QUEUE I CAN RECEIVE NEW REQUESTS
                receive();
            } else {
                // SEND NEXT COMMUNICATION IN LINE
                send(communications.poll());
            }
        }
    }

    // DISPATCH COMMUNICATION
    private void send(Communication communication) {
        try {
            cs.connect(communication.getCustomer().getHostAddresss(), 7220);
            cs.send(BytesConverter.toInputStream(communication.getPacket()));
        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // LISTEN TO NEW REQUESTS
    private void receive() {
        try {
            cs.listen();

            InputStream inputStream = cs.receive();
            Packet packet = BytesConverter.fromInputStream(inputStream);
            handle(packet);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    // PARSE REQUEST AND ACT ACCORDINGLY
    private void handle(Packet p) {
        if (p.getConnectionType().equals(ConnectionType.REGISTER)) {            // CLIENT REGISTERS IN THE SERVER
            if (!customers.containsKey(p.getPublicKey())) {
                customers.put(p.getPublicKey(), new Customer(p.getHostAddress(), p.getPublicKey()));
            }
        } else if (!customers.containsKey(p.getPublicKey())) {                  // DO NOTHING IF CLIENT IS NOT REGISTERED
            return;
        } else if (p.getConnectionType().equals(ConnectionType.ASK)) {          // CLIENT ASKED WHERE HE CAN GET FILES FROM
            tellWhere(p);
        } else if (p.getConnectionType().equals(ConnectionType.GET)) {          // CLIENT REQUESTED ME A FILE FRAGMENT
            communications.add(new Communication(buildPacket(p), customers.get(p.getPublicKey())));
        } else {
            decryptAndPut(p);
        }
    }

    private void tellWhere(Packet packet) {
        TFiles r = new TFiles();
        List<TFile> fs = packet.gettFiles().getFiles();

        fs.forEach(f -> {
            if(files.containsKey(f.getFilename())) { // IF I KNOW WHERE THE FILE IS
                r.insert(files.get(f.getFilename())); // I WILL SEND ALL INFO I HAVE ON THE FILE
            }
        });

        communications.add(new Communication(
                new Packet(ConnectionType.INFORM, r, null, null),
                customers.get(packet.getPublicKey())
        ));
    }

    // BUILD PACKET WITH FILE FRAGMENTS TO DELIVER
    private Packet buildPacket(Packet packet) {
        try {
            return new Packet(
                ConnectionType.PUT,
                getFiles(packet.gettFiles(), packet.getPublicKey()),
                GlobalVariables.getPublicKey(),
                InetAddress.getLocalHost().getHostAddress()
            );
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    // LOAD FILES TO MEMORY
    private TFiles getFiles(TFiles tFiles, PublicKey publicKey) {
        TFiles r = new TFiles();

        for(TFile t : tFiles.getFiles()) {
            try {
                File f = new File(t.getFilename());

                byte[] content = java.nio.file.Files.readAllBytes(f.toPath());
                byte[] finalData = Arrays.copyOfRange(content, t.getStart(), t.getEnd());

                String name = RSAKeys.encrypt(f.getName(), publicKey);
                r.insert(new TFile(name, RSAKeys.encrypt(finalData, publicKey)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return r;
    }

    // HANDLE A FILE UPLOAD
    private void decryptAndPut(Packet packet) {
        packet.gettFiles().getFiles().forEach(f -> {
            File file = new File(GlobalVariables.filesPath + RSAKeys.decrypt(f.getFilename(), GlobalVariables.getPrivateKey()));
            try {
                Files.write(file.toPath(), RSAKeys.decrypt(f.getFileContents(), GlobalVariables.getPrivateKey()));

                // ADD TO LIST OF FILES I HAVE
                files.put(f.getFilename(), f);

                // TELL PEERS I HAVE FILE
                for(Peer peer : peers.values()) {
                    communications.add(new Communication(
                            new Packet(ConnectionType.INFORM, null, null, address),
                            new Customer(peer.getInetAddress().getHostAddress())
                    ));
                }
            } catch ( IOException e) {
                e.printStackTrace();
            }
        });
    }
}
