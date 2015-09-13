package tempest.services;

import static org.junit.Assert.*;
import org.junit.Test;

import tempest.Machines;
import tempest.mocks.MockExecutor;
import tempest.mocks.MockLogWrapper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class LoggerTest {
    @Test
    public void loggerConstructorSetsFileHandler() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        new Logger(new Machines(), new MockExecutor(), logWrapper);
        assertEquals(1, logWrapper.addHandlerCallCount);
        assertEquals(FileHandler.class.getName(), logWrapper.lastHandler.getClass().getName());
        assertEquals(Logger.SingleLineFormatter.class.getName(), logWrapper.lastHandler.getFormatter().getClass().getName());
    }

    @Test
    public void logLineCorrectSourceClassAndSourceMethod() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        Logger logger = new Logger(new Machines(), new MockExecutor(), logWrapper);
        logger.logLine(Logger.INFO, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals("tempest.services.LoggerTest", logWrapper.lastSourceClass);
        assertEquals("logLineCorrectSourceClassAndSourceMethod", logWrapper.lastSourceMethod);
    }

    @Test
    public void logLineSevere() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        Logger logger = new Logger(new Machines(), new MockExecutor(), logWrapper);
        logger.logLine(Logger.SEVERE, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals(Level.SEVERE, logWrapper.lastLevel);
    }

    @Test
    public void logLineWarning() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        Logger logger = new Logger(new Machines(), new MockExecutor(), logWrapper);
        logger.logLine(Logger.WARNING, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals(Level.WARNING, logWrapper.lastLevel);
    }

    @Test
    public void logLineInfo() throws IOException {
        MockLogWrapper logWrapper = new MockLogWrapper();
        Logger logger = new Logger(new Machines(), new MockExecutor(), logWrapper);
        logger.logLine(Logger.INFO, "foo bar");
        assertEquals(1, logWrapper.logpCallCount);
        assertEquals(Level.INFO, logWrapper.lastLevel);
    }

    @Test
    public void getLogFileSetCorrectlyDefault() throws IOException {
        Logger logger = new Logger(new Machines(), new MockExecutor(), new MockLogWrapper());
        assertEquals("machine.1.log", logger.getLogFile());
    }

    @Test
    public void getGrepFileSetCorrectlyDefault() throws IOException {
        Logger logger = new Logger(new Machines(), new MockExecutor(), new MockLogWrapper());
        assertEquals("machine.1.log", logger.getGrepFile());
    }

    @Test
    public void getLogFileAndGrepFileSetCorrectly() throws IOException {
        Logger logger = new Logger(new MockExecutor(), new MockLogWrapper(), "logFile.log", "grepFile.log");
        assertEquals("logFile.log", logger.getLogFile());
        assertEquals("grepFile.log", logger.getGrepFile());
    }

    @Test
    public void grepDelegatesExecutor() throws IOException {
        MockExecutor executor = new MockExecutor();
        Logger logger = new Logger(new Machines(), executor, new DefaultLogWrapper());
        logger.grep("foo");
        assertEquals(1, executor.execCallCount);
    }

    @Test
    public void grepBuildsCorrectCommand() throws IOException {
        MockExecutor executor = new MockExecutor();
        Logger logger = new Logger(new Machines(), executor, new DefaultLogWrapper());
        logger.grep("foo");
        assertEquals("grep", executor.command);
        assertEquals("foo machine.1.log", executor.options);
    }

    @Test
    public void grepPrependsLogFileConcatResults() throws IOException {
        MockExecutor executor = new MockExecutor();
        executor.result = new String[]{"foo bar", "foolicious"};
        Logger logger = new Logger(new Machines(), executor, new DefaultLogWrapper());
        String expectedResult = "machine.1.log - foo bar" + System.lineSeparator()
                + "machine.1.log - foolicious" + System.lineSeparator();
        assertEquals(expectedResult, logger.grep("foo"));
    }
}
