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

    public void grepAllMachines(String grepCommand) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        Set<Future<String>> futureSet = new HashSet<Future<String>>();
        String[] machines = new String[]{"10.0.0.15"};

        for (String m : machines) {
            LOG.info(grepCommand);
            Callable<String> callable = new GrepServiceCallable(grepCommand);
            Future<String> future = pool.submit(callable);
            futureSet.add(future);
            LOG.info(m);

        }

        int j = 0;
        for (Future<String> future : futureSet) {
            try {
                LOG.info(future.get());
                j++;
            } catch (ExecutionException e) {
                LOG.error(String.valueOf(e));
                LOG.error(String.valueOf(j));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
