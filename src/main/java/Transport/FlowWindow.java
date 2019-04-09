package Transport;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

public class FlowWindow {

    private ConcurrentHashMap.KeySetView<Condition,Boolean> listeners = ConcurrentHashMap.newKeySet();
    private AtomicInteger value = new AtomicInteger(2);

    public void warn(Condition c){
        listeners.add(c);
    }

    public void purge(Condition c){
        listeners.remove(c);
    }

    public int value(){
        return value.get();
    }
}
