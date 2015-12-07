package tempest.services.spout;

import tempest.interfaces.BaseSpout;
import tempest.protos.Command;
import tempest.services.Tuple;
import tempest.services.bolt.OutputCollector;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 11/27/15.
 */
public class BaseballDataSpout implements BaseSpout {

    LinkedBlockingQueue<Tuple> queue;
    Set<Tuple> tupleSet = Collections.synchronizedSet(new HashSet<Tuple>());

    public BaseballDataSpout(LinkedBlockingQueue<Tuple> queue) {
        this.queue = queue;
    }

    public Set<Tuple> getTupleSet() {
        return tupleSet;
    }

    public static final tempest.protos.Command.Spout.SpoutType type = Command.Spout.SpoutType.BASEBALLSPOUT;

    public tempest.protos.Command.Spout.SpoutType getType() {
        return type;
    }

    public Runnable retrieveTuples() {
        return new Runnable() {
            @Override
            public void run() {

                //List<String> lines = new ArrayList<>();
                final Integer[] totalTuples = {0};

                try {
                    BufferedReader reader = new BufferedReader(new FileReader("SDFSFiles/baseballdata/Batting.csv"));
                    String line = null;
                    int count = 1;
                    try {
                        while ((line = reader.readLine()) != null) {
                            line = count + "," + line;
                            List<String> s = Arrays.asList(line.split(","));
                            Tuple t = new Tuple(s);
                            queue.put(t);
                            //tupleSet.add(t);
                            //lines.add(line);
                            totalTuples[0] += 1;

                            count++;
                        }
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                System.out.println("total Tuples " + totalTuples[0]);

//				System.out.println(lines.size());

            }
        };
    }
}