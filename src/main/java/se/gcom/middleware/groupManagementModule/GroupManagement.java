package se.gcom.middleware.groupManagementModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupManagement {
    private NamingServerCommunication namingServerCommunication;
    private HashMap<String, ArrayList<String>> groupAddressMap;

    public GroupManagement(){
        namingServerCommunication = new NamingServerCommunication();
        if (namingServerCommunication.isUp()){
            System.out.println("Up");
        } else {
            System.out.println("Down");
        }
        groupAddressMap = new HashMap<>();
    }

    public static void main(String[] args) {
        GroupManagement g = new GroupManagement();
        ArrayList<String> l = new ArrayList<>();
        l.add("127.0.0.2");
        g.createNewGroup("hej", l);
        g.groupAddressMap.clear();
        g.joinGroup("hej");
        System.out.println(g.getAddresses("hej"));
        g.shutdown();
    }
    public List<String > getAddresses(String groupName){
        return new ArrayList<>(groupAddressMap.get(groupName));
    }
    public void createNewGroup(String groupName, ArrayList<String> addresses){
        groupAddressMap.put(groupName, addresses);
        namingServerCommunication.createNewGroup(groupName, addresses);
    }

    public void joinGroup(String groupName){
        groupAddressMap.put(groupName,namingServerCommunication.getAddresses(groupName));
        namingServerCommunication.addToGroup(groupName, "127.0.0.1"); //Placeholder IP
    }

    private String getIpAddress(){
        return "jeh";
    }

    public void shutdown(){
        namingServerCommunication.shutdown();
    }

}
