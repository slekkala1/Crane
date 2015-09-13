package tempest.services;

import static org.junit.Assert.*;
import org.junit.Test;
import tempest.Machines;
import tempest.interfaces.Executor;
import tempest.interfaces.LogWrapper;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;

public class LoggerTest {
    @Test
    public void getLogfileSetCorrectly() throws IOException {
        Logger logger = new Logger(new Machines(), new MockExecutor(), new MockLogWrapper());
        assertEquals("machine.1.log", logger.getLogFile());
    }

    @Test
    public void grepDelegatesExecutor() throws IOException {
        MockExecutor executor = new MockExecutor();
        Logger logger = new Logger(new Machines(), executor, new MockLogWrapper());
        logger.grep("foo");
        assertEquals(1, executor.getExecCallCount());
    }

    @Test
    public void grepBuildsCorrectCommand() throws IOException {
        MockExecutor executor = new MockExecutor();
        Logger logger = new Logger(new Machines(), executor, new MockLogWrapper());
        logger.grep("foo");
        assertEquals("grep", executor.getCommand());
        assertEquals("foo machine.1.log", executor.getOptions());
    }

    @Test
    public void grepPrependsLogFileConcatResults() throws IOException {
        MockExecutor executor = new MockExecutor();
        executor.setResult(new String[]{"foo bar", "foolicious"});
        Logger logger = new Logger(new Machines(), executor, new MockLogWrapper());
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


        public void addHandler(Handler handler) {

        }

        public void logp(Level level, String sourceClass, String sourceMethod, String msg) {

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

        public String getCommand() {
            return command;
        }

        public int getExecCallCount() {
            return execCallCount;
        }
        public String getOptions() {
            return options;
        }

        public void setResult(String[] result) {
            this.result = result;
        }
    }
}
