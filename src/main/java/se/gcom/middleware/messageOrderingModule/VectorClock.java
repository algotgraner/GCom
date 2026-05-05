package se.gcom.middleware.messageOrderingModule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VectorClock {
    private final Map<String, Integer> clock = new ConcurrentHashMap<>();

    public void increment(String myAddress){
        // increment own spot with 1
        clock.put(myAddress, clock.getOrDefault(myAddress, 0) + 1);
    }

    public void updateFromMap(Map<String, Integer> incoming){
        if (incoming == null) return;
        // iterate over the map and take the max value of each clock
        incoming.forEach((key, value)-> {
            clock.put(key, Math.max(clock.getOrDefault(key, 0), value));
        });
    }

    public boolean canDeliver(Map<String, Integer> incomingClock, String senderID){
        // this function will have to compare the clocks,
        // this function should only be called for casual ordering
        // if the message can not be delivered it should be added in a queue in OrderingModule
        int ourClockCount = clock.getOrDefault(senderID, 0);
        int incomingClockCount = incomingClock.getOrDefault(senderID, 0);

        // we can not deliver if incoming clock has seen more
        if (incomingClockCount != ourClockCount + 1){
            System.out.println("CAN NOT DELIVER FIRST");
            return false;
        }

        // chiterate over all other processes clocks to see if they have seen something we have not
        for (Map.Entry<String, Integer> entry : incomingClock.entrySet()) {
            String process = entry.getKey();
            int incomingVal = entry.getValue();

            //if our val is less we can not deliver
            if (!process.equals(senderID)) {
                int ourVal = clock.getOrDefault(process, 0);
                if (ourVal < incomingVal) {
                    System.out.println("CAN NOT DELIVER SECOND");

                    return false;
                }
            }
        }

        return true;
    }

    public Map<String, Integer> attachClock(){
        return new ConcurrentHashMap<>(clock);
    }

    public static VectorClock fromProto(Map<String, Integer> protoMap) {
        VectorClock vc = new VectorClock();
        if (protoMap != null){
            vc.clock.putAll(protoMap);
        }
        return vc;
    }

    public String toString() {
        return clock.toString();
    }
}

