package Model;

import java.io.Serializable;
import java.util.List;

public class TFile implements Serializable {
    private String filename;
    private int start;
    private int end;
    private int total;
    private byte[] fileContents;
    private List<Peer> peers;

    public TFile(String filename) {
        this.filename = filename;
    }

    public TFile(String filename, byte[] fileContents) {
        this.filename = filename;
        this.fileContents = fileContents;
    }

    public TFile(String filename, int start, int end, int total, byte[] fileContents) {
        this.filename = filename;
        this.start = start;
        this.end = end;
        this.total = total;
        this.fileContents = fileContents;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public byte[] getFileContents() {
        return fileContents;
    }

    public void setFileContents(byte[] fileContents) {
        this.fileContents = fileContents;
    }

    public List<Peer> getPeers() {
        return peers;
    }

    public void setPeers(List<Peer> peers) {
        this.peers = peers;
    }
}
