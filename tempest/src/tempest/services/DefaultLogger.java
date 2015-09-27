package tempest.services;

import tempest.Machines;
import tempest.interfaces.Executor;
import tempest.interfaces.LogWrapper;

import java.io.*;
import java.util.Date;
import java.util.logging.*;

public class DefaultLogger implements tempest.interfaces.Logger {

    private final LogWrapper logWrapper;
    private final String logFile;
    private final String grepFile;
    private final Executor executor;

    public DefaultLogger(Machines machines, Executor executor, LogWrapper logWrapper) throws IOException {
        this(executor, logWrapper, "machine." + machines.getMachineNumber() + ".log", "machine." + machines.getMachineNumber() + ".log");
    }

    public DefaultLogger(Executor executor, LogWrapper logWrapper, String logfile, String grepFile) throws IOException {
        this.logFile = logfile;
        this.grepFile = grepFile;
        this.executor = executor;
        this.logWrapper = logWrapper;
        FileHandler fileHandler = new FileHandler(logFile);
        fileHandler.setFormatter(new SingleLineFormatter());
        logWrapper.addHandler(fileHandler);
    }

    @Override
    public void logLine(String level, String message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
        if (level.equals(SEVERE))
            logWrapper.logp(Level.SEVERE, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(WARNING))
            logWrapper.logp(Level.WARNING, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(INFO))
            logWrapper.logp(Level.INFO, stackTrace.getClassName(), stackTrace.getMethodName(), message);
    }

    @Override
    public String grep(String options) {
        String[] results = executor.exec("grep", options + " " + grepFile);
        StringBuilder resultBuilder = new StringBuilder();
        for (String line : results) {
            resultBuilder.append(line).append(System.lineSeparator());
        }
        return resultBuilder.toString();
    }

    @Override
    public String getLogFile() {
        return logFile;
    }

    @Override
    public String getGrepFile() {
        return grepFile;
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
