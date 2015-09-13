package tempest.services;

import tempest.interfaces.Executor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommandLineExecutor implements Executor {
    public String[] exec(String command, String options) {
        try {
            List<String> results = new ArrayList<>();
            Process process = Runtime.getRuntime().exec(command);
            InputStream stdout = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
            String line;
            while ((line = reader.readLine ()) != null) {
                results.add(line);
            }
            return results.toArray(new String[results.size()]);
        } catch (IOException e) {
            return null;
        }
    }
}