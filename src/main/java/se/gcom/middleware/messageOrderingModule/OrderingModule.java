package se.gcom.middleware.messageOrderingModule;

import se.gcom.middleware.Manager;
import se.gcom.middleware.communicationModule.ChatMessage;
import se.gcom.middleware.communicationModule.Message;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class OrderingModule {

    private Manager manager;
    private String myAddress;
    private final ConcurrentHashMap<String, OrderingType> groupOrdering = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VectorClock> vectorClockMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Queue<Message>> holdbackQueue = new ConcurrentHashMap<>();

    public enum OrderingType {
        UNORDERED,
        CAUSAL
    }
    public OrderingModule(Manager manager, String myAddress){
        this.manager = manager;
        this.myAddress = myAddress;
    }

    public void setUpGroup(String groupName, OrderingType type){
        groupOrdering.put(groupName, type);
        vectorClockMap.putIfAbsent(groupName, new VectorClock());
    }

    public ChatMessage handleOutgoingMessage(ChatMessage msg, String groupName){
        OrderingType type = groupOrdering.getOrDefault(groupName, OrderingType.UNORDERED);
        if (type == OrderingType.UNORDERED) {
            return msg;
        } else {
            // here we need to get and increment the vector clock
            VectorClock vc = vectorClockMap.get(groupName);
            vc.increment(myAddress);
            return msg.toBuilder().putAllVectorClock(vc.attachClock()).build();
        }

    }


    public void handleIncomingMessage(ChatMessage msg){
        OrderingType type = groupOrdering.get(msg.getGroupId());
        if (type == null){
            System.out.println("No message ordering type set, defaulting to UNORDERED");
            type = OrderingType.UNORDERED;
        }


        switch (type){
            case OrderingType.CAUSAL:
                System.out.println("CAUSAL not implemented yet!");
                break;

            case OrderingType.UNORDERED:

                // just receive right away
                manager.deliverIncomingMessage(msg);
                break;

            default:
                System.err.println("Error default case reached");
                break;

        }
    }




}
