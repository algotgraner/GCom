package se.gcom.middleware.groupManagementModule;

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
        namingServerIsUp = namingServerCommunication.isUp();
        if (namingServerCommunication.isUp()){
            namingServerIsUp = true;
        } else{
            throw new NamingServerIsDown();
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
        if(namingServerIsUp) {
            addresses.add(this.address);
            namingServerCommunication.createNewGroup(groupName, addresses);
        }
    }

    public void joinGroup(String groupName){
        groupAddressMap.put(groupName,namingServerCommunication.getAddresses(groupName));
        namingServerCommunication.addToGroup(groupName, this.address); //Placeholder IP
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

    public static class NamingServerIsDown extends RuntimeException {
        public NamingServerIsDown() {
            super("Naming server is down");
        }
    }

}
