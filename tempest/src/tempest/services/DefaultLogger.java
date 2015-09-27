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
        logWrapper.addFileHandler(logFile);
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
}
