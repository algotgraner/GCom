package se.NameServer;

import java.util.ArrayList;
import java.util.HashMap;

public class NamingServer {
    private HashMap<String, ArrayList<String>> groupAddressMap = new HashMap<>();


    public ArrayList<String> getAddresses(String groupName) {
        return groupAddressMap.get(groupName);
    }
    public void addNewGroup(String groupName, ArrayList<String> addresses){
        groupAddressMap.put(groupName, addresses);
    }
    public void addToGroup(String groupName, String address){
        groupAddressMap.get(groupName).add(address);
    }
}
