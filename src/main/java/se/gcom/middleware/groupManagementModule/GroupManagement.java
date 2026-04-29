package se.gcom.middleware.groupManagementModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupManagement {
    private NamingServerCommunication namingServerCommunication;
    private HashMap<String, ArrayList<String>> groupAddressMap;
    private String address;

    public GroupManagement(int port){
        namingServerCommunication = new NamingServerCommunication();
        if (namingServerCommunication.isUp()){
            System.out.println("Up");
        } else {
            System.out.println("Down");
        }
        groupAddressMap = new HashMap<>();
        String ip = getIpAddress();
        address = ip + ":" + port;
        groupAddressMap.put("Group1", new ArrayList<>(List.of("127.0.0.1:5001")));
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
        namingServerCommunication.addToGroup(groupName, this.address); //Placeholder IP
    }

    public void leaveGroup(String groupName){
        namingServerCommunication.removeFromGroup(groupName, this.address);
        groupAddressMap.remove(groupName);
    }

    private String getIpAddress(){
        return "127.0.0.1";
    }

    public void shutdown(){
        namingServerCommunication.shutdown();
    }

}
