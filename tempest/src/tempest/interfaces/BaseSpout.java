package tempest.interfaces;

import tempest.services.Tuple;

import java.util.Set;

/**
 * Created by swapnalekkala on 11/27/15.
 */
public interface BaseSpout {
    Runnable retrieveTuples();
    tempest.protos.Command.Spout.SpoutType getType();
    Set<Tuple> getTupleSet();
}
