import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by slekkala on 9/8/15.
 */
public class GrepClient {
    private static final Logger LOG = LoggerFactory.getLogger(GrepClient.class);

    private static ExecutorService pool = Executors.newFixedThreadPool(7);
    String[] machines;

    public GrepClient(String[] machines) {
        this.machines = machines;
    }

    public void grepAllMachines(String grepCommand) throws IOException {
        Set<Future<String>> futureSet = new HashSet<Future<String>>();

        for (String m : this.machines) {
            Callable<String> callable = new GrepServiceCallable("grep -H " + grepCommand + " *.log", m);
            Future<String> future = pool.submit(callable);
            futureSet.add(future);
        }

        for (Future<String> future : futureSet) {
            try {
                future.get();
            } catch (ExecutionException e) {
                LOG.error(String.valueOf(e));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
