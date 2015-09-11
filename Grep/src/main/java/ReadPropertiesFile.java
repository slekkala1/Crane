import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by slekkala on 9/9/15.
 */
public class ReadPropertiesFile {
    private static final Logger LOG = LoggerFactory.getLogger(ReadPropertiesFile.class);

    /*
     * @description This method reads from config.properties file and returns the comma separated server hostnames as string
     */
    public String readPropertiesFile() throws IOException {
        Properties prop = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file 'config.properties' not found in the classpath");
            }
        } catch (Exception e) {
            LOG.error("Exception: " + e);
        }
        return prop.getProperty("servers");
    }
}
