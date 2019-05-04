package Test;

import Common.*;
import Model.*;
import Transport.GCVSocket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

class Client {
    private GCVSocket cs;
    private RSAKeys myKeys;
    private PublicKey destinyPublicKey = GlobalVariables.getPublicKey();
    private String address;

    Client(GCVSocket cs) {
        this.cs = cs;
        this.myKeys = new RSAKeys();
        this.myKeys.generate();

        try {
            this.address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    void register(InetAddress inetAddress) {
        try {
            cs.connect(inetAddress, 7220);
            Packet packet = new Packet(ConnectionType.REGISTER, null, myKeys.getPublicKey(), address);
            cs.send(BytesConverter.toInputStream(packet));
        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    void put(InetAddress inetAddress, List<String> filenames) {
        List<File> files = new ArrayList<>();
        filenames.forEach(f -> files.add(new File(f)));
        sendFiles(inetAddress, files);
    }

    void get(InetAddress inetAddress, List<String> filenames) {
        TFiles tFiles = new TFiles();
        filenames.forEach(f -> tFiles.insert(new TFile(f)));

        try {
            cs.connect(inetAddress, 7220);
            Packet packet = new Packet(ConnectionType.ASK, tFiles, myKeys.getPublicKey(), address);
            cs.send(BytesConverter.toInputStream(packet));

            //TODO: GET FROM ALL IN PARALLEL
            InputStream inputStream = cs.receive();
            Packet p = BytesConverter.fromInputStream(inputStream);

            for(TFile f : p.gettFiles().getFiles()) {
                f.setStart(0);
                f.setEnd(f.getTotal());

                TFiles tfs = new TFiles();
                tfs.insert(f);
                Packet a = new Packet(ConnectionType.GET, tfs, myKeys.getPublicKey(), address);

                //FIXME: GETTING FROM FIRST SERVER WITH FILE ONLY
                cs.connect(f.getPeers().get(0).getInetAddress(), 7220);
                cs.send(BytesConverter.toInputStream(a));
            }

        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }


    // PRIVATE


    private void sendFiles(InetAddress inetAddress, List<File> files)  {
        try {
            TFiles fs = new TFiles();

            for (File file : files) {
                byte[] content = RSAKeys.encrypt(java.nio.file.Files.readAllBytes(file.toPath()), destinyPublicKey);
                String name = RSAKeys.encrypt(file.getName(), destinyPublicKey);
                fs.insert(new TFile(name, content));
            }

            Packet p = new Packet(ConnectionType.PUT, fs, myKeys.getPublicKey(), address);
            sendTFiles(inetAddress, p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTFiles(InetAddress inetAddress, Packet files) {
        try {
            cs.connect(inetAddress, 7220);
            cs.send(BytesConverter.toInputStream(files));
        } catch (IOException | InterruptedException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
