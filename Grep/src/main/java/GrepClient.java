import asg.cliche.ShellFactory;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by slekkala on 9/8/15.
 */
public class GrepClient {


    private static final Logger LOG = Logger.getLogger(GrepClient.class);

    public static void grepAllMachines(String grepCommand) throws IOException {

        ExecutorService pool = Executors.newFixedThreadPool(1);
        Set<Future<String>> futureSet = new HashSet<Future<String>>();
        String[] machines = new String[] {"10.0.0.15"};


        for(String m:machines) {
            LOG.info(grepCommand);
            Callable<String> callable = new GrepServiceCallable(grepCommand);
            Future<String> future = pool.submit(callable);
            futureSet.add(future);
            LOG.info(m);

        }
        int j=0;
        for (Future<String> future : futureSet) {
            try {
                LOG.info(0);
                LOG.info(future.get());
                j++;
            }catch(ExecutionException e) {
                LOG.error(String.valueOf(e));
                LOG.error(String.valueOf(j));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        BasicConfigurator.configure();
        Console console = new Console(new GrepClient());
        ShellFactory.createConsoleShell("CS425-MP-Lekkala-Morrow", "", console).commandLoop();

    }

}
