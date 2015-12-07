package tempest.services.spout;

import tempest.interfaces.BaseSpout;
import tempest.protos.Command;
import tempest.services.Tuple;
import tempest.services.bolt.OutputCollector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 11/27/15.
 */
public class StockDataSpout implements BaseSpout {

    LinkedBlockingQueue<Tuple> queue;

    public StockDataSpout(LinkedBlockingQueue<Tuple> queue) {
        this.queue = queue;
    }

    public static final tempest.protos.Command.Spout.SpoutType type = Command.Spout.SpoutType.STOCKDATASPOUT;

    public tempest.protos.Command.Spout.SpoutType getType() {
        return type;
    }

    public Runnable retrieveTuples() {
        return new Runnable() {
            @Override
            public void run() {

                File myFile = new File("SDFSFiles/quant");
                try {

                    List<String> lines = new ArrayList<>();

                    if (myFile.exists()) {
                        Files.walk(Paths.get("SDFSFiles/quant")).forEach(filePath -> {
                            if (Files.isRegularFile(filePath)) {
                                System.out.println(filePath);
                                BufferedReader reader = null;
                                try {
                                	String name = filePath.toString().substring(filePath.toString().lastIndexOf('_')+1);
                                    reader = new BufferedReader(new FileReader(filePath.toFile()));

                                    String line = null;
                                    int count = 1;
                                    try {
                                        while ((line = reader.readLine()) != null) {
                                            line = count + "," + line;
                                            List<String> s = Arrays.asList(line.split(","));
                                            s.add(name);
                                            Tuple t = new Tuple(s);
                                            queue.put(t);
                                            lines.add(line);
                                            count++;
                                        }
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        //System.out.println(lines.get(0));
                        System.out.println(lines.size());
                        //System.out.println(lines.get(lines.size() - 1));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}