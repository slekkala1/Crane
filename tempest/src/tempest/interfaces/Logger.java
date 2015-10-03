package tempest.interfaces;

public interface Logger {
    String INFO = "info";
    String WARNING = "warning";
    String SEVERE = "severe";

    void logLine(String level, String message);
    String grep(String options);
    String getLogFile();
    String getGrepFile();
}
