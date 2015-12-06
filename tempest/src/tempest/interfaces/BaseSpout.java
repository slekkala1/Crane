package tempest.interfaces;

/**
 * Created by swapnalekkala on 11/27/15.
 */
public interface BaseSpout {
    Runnable retrieveTuples();
    tempest.protos.Command.Spout.SpoutType getType();
}
