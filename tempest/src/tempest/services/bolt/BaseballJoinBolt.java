package tempest.services.bolt;

import tempest.services.Tuple;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by swapnalekkala on 12/2/15.
 */
public class BaseballJoinBolt
        implements Callable {
    LinkedBlockingQueue<Tuple> queue;
    OutputCollector outputCollector;
    Map<String, String> nameMap;

    public BaseballJoinBolt(LinkedBlockingQueue queue, OutputCollector outputCollector) {
        this.queue = queue;
        this.outputCollector = outputCollector;
        this.nameMap = new HashMap<String, String>();
        generateNameMap();
    }
    
    private void generateNameMap() {
    	try {
			BufferedReader reader = new BufferedReader(new FileReader("SDFSFiles/baseballdata/Master.csv"));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					List<String> s = Arrays.asList(line.split(","));
					String id = s.get(1);
					String fName = s.get(14);
					String lName = s.get(15);
					nameMap.put(id, fName + " " + lName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    }

    public Tuple call() {
        Tuple tuple = null;
        try {
            if (!outputCollector.member.getHost().equals("")) {
                while ((tuple = queue.poll(1000, TimeUnit.MILLISECONDS)) != null) {
                	List<String> list = tuple.getStringList();
                	List<String> shortened = new ArrayList<String>();
                	String name = nameMap.get(list.get(0));
                	if (name != null) {
                		shortened.add(name);
                		shortened.add(list.get(0));
                		tuple.setStringList(shortened);
                		outputCollector.add(tuple);
                	}
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return tuple;
    }
}
