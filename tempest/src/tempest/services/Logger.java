package tempest.services;

import tempest.Machines;

import java.io.*;
import java.util.Date;
import java.util.logging.*;

public class Logger {
    public static final String INFO = "info";
    public static final String WARNING = "warning";
    public static final String SEVERE = "severe";

    private final java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();
    private final String logFile;

    public Logger(Machines machines) throws IOException {
        logFile = "machine." + machines.getMachineNumber() + ".log";
        FileHandler fileHandler = new FileHandler(logFile);
        fileHandler.setFormatter(new SingleLineFormatter());
        logger.addHandler(fileHandler);
        logger.setUseParentHandlers(true);
    }

    public void logLine(String level, String message) {
        StackTraceElement stackTrace = Thread.currentThread().getStackTrace()[2];
        if (level.equals(SEVERE))
            logger.logp(Level.SEVERE, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(WARNING))
            logger.logp(Level.WARNING, stackTrace.getClassName(), stackTrace.getMethodName(), message);
        if (level.equals(INFO))
            logger.logp(Level.INFO, stackTrace.getClassName(), stackTrace.getMethodName(), message);
    }

    public String grep(String options) {
        String command = "grep " + options + " " + logFile;
        try {
            Process process = Runtime.getRuntime().exec(command);
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            StringBuilder resultBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine ()) != null) {
                resultBuilder.append(logFile).append(" - ").append(line).append(System.getProperty("line.separator"));
            }
            return resultBuilder.toString();
        } catch (IOException e) {
            logLine(SEVERE, "IOException while grepping" + e);
            return "Grep failed.";
        }
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