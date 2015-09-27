package tempest.interfaces;

import java.io.IOException;
import java.util.logging.Level;

public interface LogWrapper {
    void addFileHandler(String file) throws IOException;
    void logp(Level level, String sourceClass, String sourceMethod, String msg);
}
