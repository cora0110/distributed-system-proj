package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ServerLog {
    /**
     * Gets the timestamp with the current system time in millisecond precision and a human-readable time format
     * @return a Date in a date/time string
     */
    public static String getCurrentTimestamp() {
        Date currDate = new Date(System.currentTimeMillis());
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z");
        return formatter.format(currDate);
    }

    /**
     * Prints a message to the server log timestamped with the current system time
     * @param message message that needs to print out
     */
    public void log(String message) {
        System.out.println("[" + getCurrentTimestamp() + "]  " + message);
    }

}
