package pt.tecnico.distledger.namingserver;

import java.util.*;


public class ServiceEntry {

    // irá guardar o nome de um serviço e um conjunto de ServerEntries. 
    private String serviceName;
    private List<ServerEntry> serverEntries;

    public ServiceEntry(String serviceName){
        this.serviceName = serviceName;
        this.serverEntries = new ArrayList<>();
    }

    public ServiceEntry(String serviceName, ServerEntry serverEntry){
        this.serviceName = serviceName;
        this.serverEntries = new ArrayList<>();
        addServerEntry(serverEntry);
    }

    public void addServerEntry (ServerEntry serverEntry){
        if (!serverEntries.contains(serverEntry)){
            serverEntries.add(serverEntry);
        }
    }

    public List<ServerEntry> getServerEntries(){
        return serverEntries;
    }

    public int removeServerEntry(String serverAddress){
        ServerEntry serverEntry;
        List<ServerEntry> serverEntries = getServerEntries();
        for (int i = 0; i < serverEntries.size(); i++) {
            serverEntry = serverEntries.get(i);
            if (serverEntry.getServerAddress().equals(serverAddress)){
                this.serverEntries.remove(i);
                return 0;
            }
        }
        return -1;
    }
}
