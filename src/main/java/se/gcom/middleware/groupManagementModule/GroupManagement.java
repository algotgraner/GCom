package se.gcom.middleware.groupManagementModule;

import io.grpc.StatusRuntimeException;
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
    }

    public boolean NamingServerIsUp() {
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
        addresses.add(this.address);
        if(namingServerIsUp) {
            namingServerCommunication.createNewGroup(groupName, addresses);
        }
    }

    public ArrayList<String> joinGroup(String groupName) throws StatusRuntimeException {
        ArrayList<String> addresses = namingServerCommunication.getAddresses(groupName);
        ArrayList<String> a = new ArrayList<>(addresses);
        a.add(this.address);
        groupAddressMap.put(groupName, a);
        namingServerCommunication.addToGroup(groupName, this.address);
        return addresses;
    }
    public void joinGroup(String groupName, ArrayList<String> addresses){
        groupAddressMap.put(groupName, new ArrayList<>(addresses));
    }

    public void leaveGroup(String groupName){
        if (namingServerIsUp){
            namingServerCommunication.removeFromGroup(groupName, this.address);
        }
        groupAddressMap.remove(groupName);
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
    public String getAddress(){
        return address;
    }

    public void addNewMember(String groupName, String ipAddress){
        groupAddressMap.get(groupName).add(ipAddress);
    }
    public void removeMember(String groupName, String ipAddress){
        groupAddressMap.get(groupName).remove(ipAddress);
    }


    public void shutdown(){
        namingServerCommunication.shutdown();
    }

}
