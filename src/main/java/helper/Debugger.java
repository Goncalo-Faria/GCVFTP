package helper;

public class Debugger {
    private static boolean enabled = true;

    public static void setEnabled(boolean enabled) {
        Debugger.enabled = enabled;
    }

    public static void log(Object o) {
        if(enabled)
            System.out.println("::Debugger:: ---> " + o.toString());
    }
}