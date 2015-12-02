package tempest.services;

import java.io.Serializable;
import java.util.List;

/**
 * Created by swapnalekkala on 11/27/15.
 */
public class Tuple implements Serializable {
    private List<String> stringList;

    public Tuple(List<String> stringList) {
        this.stringList = stringList;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }
}
