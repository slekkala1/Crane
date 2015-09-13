package tempest.services;

import tempest.Machines;
import tempest.interfaces.Executor;
import tempest.interfaces.LogWrapper;

import java.io.*;
import java.util.Date;
import java.util.logging.*;

public class Logger {
    public static final String INFO = "info";
    public static final String WARNING = "warning";
    public static final String SEVERE = "severe";

    private final LogWrapper logWrapper;
    private final String logFile;
    private final Executor executor;

    public Logger(Machines machines, Executor executor, LogWrapper logWrapper) throws IOException {
        logFile = "machine." + machines.getMachineNumber() + ".log";
        this.executor = executor;
        this.logWrapper = logWrapper;
        FileHandler fileHandler = new FileHandler(logFile);
        fileHandler.setFormatter(new SingleLineFormatter());
        logWrapper.addHandler(fileHandler);
    }

    public void logLine(String level, String message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
        if (level.equals(SEVERE))
            logWrapper.logp(Level.SEVERE, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(WARNING))
            logWrapper.logp(Level.WARNING, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(INFO))
            logWrapper.logp(Level.INFO, stackTrace.getClassName(), stackTrace.getMethodName(), message);
    }

    public String grep(String options) {
        String[] results = executor.exec("grep", options + " " + logFile);
        StringBuilder resultBuilder = new StringBuilder();
        for (String line : results) {
            resultBuilder.append(logFile).append(" - ").append(line).append(System.getProperty("line.separator"));
        }
        return resultBuilder.toString();
    }

    public String getLogFile() {
        return logFile;
    }

    class SingleLineFormatter extends Formatter {

        private static final String format = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n";
        private final Date dat = new Date();

        public synchronized String format(LogRecord record) {
            dat.setTime(record.getMillis());
            String source;
            if (record.getSourceClassName() != null) {
                source = record.getSourceClassName();
                if (record.getSourceMethodName() != null) {
                    source += " " + record.getSourceMethodName();
                }
            } else {
                source = record.getLoggerName();
            }
            String message = formatMessage(record);
            String throwable = "";
            if (record.getThrown() != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                pw.println();
                record.getThrown().printStackTrace(pw);
                pw.close();
                throwable = sw.toString();
            }
            return String.format(format,
                    dat,
                    source,
                    record.getLoggerName(),
                    record.getLevel().getName(),
                    message,
                    throwable);
        }
    }
}
