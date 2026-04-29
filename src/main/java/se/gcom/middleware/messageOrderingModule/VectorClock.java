package se.gcom.middleware.messageOrderingModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorClock {
    private final Map<String, Integer> clock = new ConcurrentHashMap<>();


    public void increment(String myID){
        // increment own spot with 1
        clock.put(myID, clock.getOrDefault(myID, 0) + 1);
    }

    public boolean canDeliver(Map<String, Integer> incomingClock, String senderID){
        // this function will have to compare the clocks,
        // this function should only be called for casual ordering
        // if the message can not be delivered it should be added in a queue in OrderingModule
        return true;
    }

    public Map<String, Integer> attachClock(){
        return new ConcurrentHashMap<>(clock);
    }
}
