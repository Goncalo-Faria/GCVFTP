package Common;

public class Debugger {
    private static boolean enabled = false;

    public static void setEnabled(boolean enabled) {
        Debugger.enabled = enabled;
    }

    public static void log(String str) {
        if(enabled)
            System.out.println(str);
    }
}
