import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by slekkala on 9/9/15.
 */
public class ReadPropertiesFile {

    public String readPropertiesFile() throws IOException {
        InputStream inputStream = null;
        Properties prop = new Properties();

        try {

            //Read properties file
        String propFileName = "config.properties";

        inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
        }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            inputStream.close();
        }
        // get the property value and return it
        return prop.getProperty("servers");
    }
}
