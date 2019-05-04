package Test;

import Common.GlobalVariables;

public class RunServer {
    public static void main(String[] args) {
        if(args.length > 0) {
            GlobalVariables.filesPath = args[0];
        }

        new Thread(new Server()).start();
    }
}
