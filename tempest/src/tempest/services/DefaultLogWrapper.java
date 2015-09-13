package tempest.services;

import tempest.interfaces.LogWrapper;

import java.util.logging.Handler;
import java.util.logging.Level;

public class DefaultLogWrapper implements LogWrapper {
    private final java.util.logging.Logger logger = java.util.logging.Logger.getGlobal();

    public void addHandler(Handler handler) {
        logger.addHandler(handler);
    }

    public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
        logger.logp(level, sourceClass, sourceMethod, msg);
    }
}
