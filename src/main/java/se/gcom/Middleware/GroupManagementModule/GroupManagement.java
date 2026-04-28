package se.gcom.Middleware.GroupManagementModule;

import java.util.ArrayList;
import java.util.HashMap;

public class GroupManagement {
    private NamingServerCommunication namingServerCommunication;
    private HashMap<String, ArrayList<String>> groupAddressMap = new HashMap<>();

    public GroupManagement(){
        namingServerCommunication = new NamingServerCommunication();
        if (namingServerCommunication.isUp()){
            System.out.println("Up");
        } else {
            System.out.println("Down");
        }
    }

    public static void main(String[] args) {
        GroupManagement g = new GroupManagement();
        g.shutdown();
    }
    public void shutdown(){
        namingServerCommunication.shutdown();
    }

}
