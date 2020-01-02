package hec.firo;

public class Logger {

    static boolean DebugMode = false;

    static void log(String msg) {
        if (DebugMode)
            System.out.println(msg);
    }

    static void logWarning(String msg) {
        log("Warning: " + msg);
    }
    static void logError(String msg) {
        log("Error: " + msg);
    }
}
