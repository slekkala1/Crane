package tempest.services;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by swapnalekkala on 12/6/15.
 */
public class TupleService {
    private List<Integer> ackedIds;
    private LinkedBlockingQueue<Tuple> queue;

    public TupleService(List<Integer> id, LinkedBlockingQueue<Tuple> queue) {
        this.ackedIds = id;
        this.queue = queue;
    }

    //redo logic in Topology handler for the tuple list that is not acknowledged




}
