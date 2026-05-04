package se.gcom.middleware.groupManagementModule;

import se.gcom.middleware.communicationModule.GroupMembership;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupManagement {
    private NamingServerCommunication namingServerCommunication;
    private HashMap<String, ArrayList<String>> groupAddressMap;
    private String address;
    private boolean namingServerIsUp;

    public GroupManagement(int port){
        namingServerCommunication = new NamingServerCommunication();
        groupAddressMap = new HashMap<>();
        String ip = getIpAddress();
        address = ip + ":" + port;
        namingServerIsUp = namingServerCommunication.isUp();
        groupAddressMap.put("Group1", new ArrayList<>(List.of("172.20.10.2:5001")));

    }

    public boolean isNamingServerIsUp() {
        return namingServerIsUp;
    }

    public List<String > getAddresses(String groupName){
        ArrayList<String> addresses = groupAddressMap.get(groupName);
        if (addresses == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(addresses);
    }
    public void createNewGroup(String groupName, ArrayList<String> addresses){
        groupAddressMap.put(groupName, addresses);
        if(namingServerIsUp) {
            addresses.add(this.address);
            namingServerCommunication.createNewGroup(groupName, addresses);
        }
    }

    public ArrayList<String> joinGroup(String groupName){
        ArrayList<String> addresses = namingServerCommunication.getAddresses(groupName);
        groupAddressMap.put(groupName, addresses);
        namingServerCommunication.addToGroup(groupName, this.address);
        return addresses;
    }
    public void joinGroup(String groupName, ArrayList<String> addresses){
        groupAddressMap.put(groupName, addresses);
    }

    public void leaveGroup(String groupName){
        if (namingServerIsUp){
            namingServerCommunication.removeFromGroup(groupName, this.address);
        }
        groupAddressMap.get(groupName).remove(this.address);
    }

    private String getIpAddress(){
        String ip = null;
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
            System.out.println("Local IP: " + ip);
        } catch (SocketException | UnknownHostException e) {
            System.out.println(e.getMessage());
        }
        return ip;
    }


    public void shutdown(){
        namingServerCommunication.shutdown();
    }

}
