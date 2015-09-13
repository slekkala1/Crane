package tempest.interfaces;

import java.util.logging.Handler;
import java.util.logging.Level;

public interface LogWrapper {
    void addHandler(Handler handler);
    void logp(Level level, String sourceClass, String sourceMethod, String msg);
}
