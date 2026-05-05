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
    private final ConcurrentHashMap<String, Queue<ChatMessage>> holdbackQueue = new ConcurrentHashMap<>();

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
        if (type == OrderingType.CAUSAL) {
            // create vector clock
            VectorClock vc = new VectorClock();
            vc.increment(myAddress);
            vectorClockMap.put(groupName, vc);
            holdbackQueue.putIfAbsent(groupName, new ConcurrentLinkedQueue<>());
        }
    }

    public void joinGroup(String groupName, Map<String, Integer> groupVectorClock){
        OrderingType type = groupOrdering.getOrDefault(groupName, OrderingType.UNORDERED);

        if (type == OrderingType.CAUSAL) {
            VectorClock vc = new VectorClock();
            if (groupVectorClock != null && !groupVectorClock.isEmpty()) {
                vc.updateFromMap(groupVectorClock);
            }
            vc.increment(myAddress);
            vectorClockMap.put(groupName, vc);
            holdbackQueue.putIfAbsent(groupName, new ConcurrentLinkedQueue<>());
            System.out.println("[Ordering] Joined CAUSAL group: " + groupName +" VC: " + vc);
        } else {
            System.out.println("[Ordering] Joined UNORDERED group: " + groupName );
        }
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

        if (type == OrderingType.UNORDERED) {
            manager.deliverIncomingMessage(msg);
        } else {
            handleCausalIncoming(msg);
        }
    }

    private void handleCausalIncoming(ChatMessage msg){
        String groupId = msg.getGroupId();
        VectorClock myVC = vectorClockMap.get(groupId);
        Queue<ChatMessage> queue = holdbackQueue.get(groupId);

        if (myVC == null || queue == null) {
            System.err.println("ERROR: myVC or holdback queue is null, we deliver WITHOUT checking tho");
            manager.deliverIncomingMessage(msg);
            return;
        }

        Map<String, Integer> incomingClock = msg.getVectorClockMap();

        System.out.println("From: " + msg.getSenderId() + ", message ID: " + msg.getMessageId());
        System.out.println("Current  VC (my clock): " + myVC);
        System.out.println("Incoming VC: " + incomingClock);
        System.out.println("Can deliver?: " + myVC.canDeliver(incomingClock, msg.getSenderId()));
        System.out.println("Holdback queue size: " + queue.size());

        if (myVC.canDeliver(incomingClock, msg.getSenderId())) {
            // deliver immediately
            manager.deliverIncomingMessage(msg);
            // update oru clock
            myVC.updateFromMap(incomingClock);

            // check holdback queue for newly deliverable messages
            //checkHoldbackQueue(groupId) needs to be implemented
        } else {
            // put at holdback queue
            queue.add(msg);
            System.out.println("Message " + msg.getMessageId() +" held back, current holdback size: " + queue.size());
        }
    }


    public Map<String, Integer> getVectorClock(String groupName) {
        VectorClock vc = vectorClockMap.get(groupName);
        if (vc != null) {
            return vc.attachClock();
        }
        System.out.println("No vector clock for group: " + groupName);
        return new ConcurrentHashMap<>();
    }

    public Boolean orderingIsCausal(String groupName){
        OrderingType type = groupOrdering.get(groupName);
        if (type == OrderingType.CAUSAL) {
            return true;
        } else {
            return false;
        }
    }




}
