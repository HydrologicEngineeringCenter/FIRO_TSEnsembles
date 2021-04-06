package hec.ensemble;

public class Logger {

    static boolean DebugMode = false;

    public static void log(String msg) {
        if (DebugMode)
            System.out.println(msg);
    }

    public static void logWarning(String msg) {
        log("Warning: " + msg);
    }
    public static void logError(String msg) {
        log("Error: " + msg);
    }
    public static void logError(Exception e) {
        log("Error: " + e.getMessage());
        e.printStackTrace(System.out);

    }
}
