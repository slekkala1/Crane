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

    LinkedBlockingQueue queue = new LinkedBlockingQueue();

    public StockDataSpout(LinkedBlockingQueue queue) {
        this.queue = queue;
    }

    public static final tempest.protos.Command.Spout.SpoutType type = Command.Spout.SpoutType.STOCKDATASPOUT;

    public tempest.protos.Command.Spout.SpoutType getType() {
        return type;
    }

    public String tuplesFromFile(String fileName) {

        File myFile = new File("/Users/swapnalekkala/Downloads/quantquote/daily/");
        try {

            if (myFile.exists()) {
                Files.walk(Paths.get("/Users/swapnalekkala/Downloads/quantquote/daily/")).forEach(filePath -> {
                    if (Files.isRegularFile(filePath)) {
                        System.out.println(filePath);
                    }
                });

                //logger.logLine(logger.INFO, "Get chunkName " + chunkName + " from Server " + Inet4Address.getLocalHost().getHostName().toString());
                BufferedReader reader = new BufferedReader(new FileReader(myFile));
                List<String> lines = new ArrayList<>();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                System.out.println(lines.get(0));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public Runnable tuplesFromFile1(LinkedBlockingQueue<Tuple> queue, String fileName) {
        return new Runnable() {
            @Override
            public void run() {

                File myFile = new File("/Users/swapnalekkala/Downloads/quantquote/daily/");
                try {

                    List<String> lines = new ArrayList<>();

                    lines.add("DATE, TIME, OPEN, HIGH, LOW, CLOSE, VOLUME");

                    if (myFile.exists()) {
                        Files.walk(Paths.get("/Users/swapnalekkala/Downloads/quantquote/daily1/")).forEach(filePath -> {
                            if (Files.isRegularFile(filePath)) {
                                System.out.println(filePath);
                                BufferedReader reader = null;
                                try {
                                    reader = new BufferedReader(new FileReader(filePath.toFile()));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                String line = null;
                                int count = 1;
                                try {
                                    while ((line = reader.readLine()) != null) {
                                        line = count + "," + line;
                                        List<String> s = Arrays.asList(line.split(","));
                                        Tuple t = new Tuple(s);
                                        queue.put(t);
                                        lines.add(line);
                                        count++;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
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
//       // public static void main (String[]args){
//            StockDataSpout.tuplesFromFile1("xyz");
//        }
}