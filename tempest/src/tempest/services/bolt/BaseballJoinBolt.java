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
public class BaseballJoinBolt implements Callable {
	LinkedBlockingQueue<Tuple> queue;
	private List<OutputCollector> outputCollectorList;
	static Map<String, String> nameMap = generateNameMap();

	public BaseballJoinBolt(LinkedBlockingQueue queue,
			List<OutputCollector> outputCollectorList) {
		this.queue = queue;
		this.outputCollectorList = outputCollectorList;
	}

	private static Map<String, String> generateNameMap() {
		Map<String, String> nameMap = new HashMap<String, String>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(
					"SDFSFiles/baseballdata/Master.csv"));
			String line = null;
			try {
				while ((line = reader.readLine()) != null) {
					List<String> s = Arrays.asList(line.split(","));
					String id = s.get(0);
					String fName = s.get(13);
					String lName = s.get(14);
					nameMap.put(id, fName + " " + lName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return nameMap;
	}

	public Tuple call() {
		Tuple tuple = null;
		try {
			while ((tuple = queue.poll(1000, TimeUnit.MILLISECONDS)) != null) {
				List<String> list = tuple.getStringList();
				List<String> shortened = new ArrayList<String>();
				String name = nameMap.get(list.get(1));
				if (name != null) {
					shortened.add(name);
					shortened.add(list.get(0));
					shortened.add(list.get(9));
					tuple.setStringList(shortened);
					for (int i = 0; i < outputCollectorList.size(); i++) {
						outputCollectorList.get(i).add(tuple);
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return tuple;
	}
}
