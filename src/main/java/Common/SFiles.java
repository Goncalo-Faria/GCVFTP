package Common;

import java.util.*;

public class SFiles {
    private Map<String, List<String>> files;

    public SFiles() {
        this.files = new HashMap<>();
    }

    public void add(String file, String address) {
        if(files.containsKey(file)) {
            files.get(file).add(address);
        } else {
            List<String> s = new ArrayList<>();
            s.add(address);
            files.put(file, s);
        }
    }

    public List<String> get(String file) {
        return files.getOrDefault(file, new ArrayList<>());
    }
}
