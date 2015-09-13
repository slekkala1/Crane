package tempest.services;

import static org.junit.Assert.*;
import org.junit.Test;
import tempest.Machines;
import tempest.interfaces.Executor;
import tempest.interfaces.LogWrapper;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
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
    public void getLogfileSetCorrectly() throws IOException {
        Logger logger = new Logger(new Machines(), new MockExecutor(), new MockLogWrapper());
        assertEquals("machine.1.log", logger.getLogFile());
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
        String expectedResult = "machine.1.log - foo bar" + System.getProperty("line.separator")
                + "machine.1.log - foolicious" + System.getProperty("line.separator");
        assertEquals(expectedResult, logger.grep("foo"));
    }

    class MockLogWrapper implements LogWrapper {
        private int addHandlerCallCount;
        private int logpCallCount;
        private Handler lastHandler;
        private Level lastLevel;
        private String lastSourceClass;
        private String lastSourceMethod;
        private String lastMsg;

        public void addHandler(Handler handler) {
            ++addHandlerCallCount;
            lastHandler = handler;
        }

        public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
            ++logpCallCount;
            lastLevel = level;
            lastSourceClass = sourceClass;
            lastSourceMethod = sourceMethod;
            lastMsg = msg;
        }
    }

    class MockExecutor implements Executor {
        private int execCallCount;
        private String command;
        private String options;
        private String[] result = new String[0];

        public String[] exec(String command, String options) {
            ++execCallCount;
            this.command = command;
            this.options = options;
            return result;
        }
    }
}
