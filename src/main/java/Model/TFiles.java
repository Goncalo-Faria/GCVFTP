package Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TFiles implements Serializable {
    private List<TFile> files;

    public TFiles() {
        files = new ArrayList<>();
    }

    public void insert(TFile file) {
        files.add(file);
    }

    public List<TFile> getFiles() {
        return files;
    }

    public void setFiles(List<TFile> files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return "TFiles{" +
                "files=" + files +
                '}';
    }
}
