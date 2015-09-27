package tempest.interfaces;

/**
 * Created by loren on 9/26/2015.
 */
public interface Logger {
    String INFO = "info";
    String WARNING = "warning";
    String SEVERE = "severe";

    void logLine(String level, String message);

    String grep(String options);

    String getLogFile();

    String getGrepFile();
}
