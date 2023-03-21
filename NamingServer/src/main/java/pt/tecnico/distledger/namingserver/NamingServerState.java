package pt.tecnico.distledger.namingserver;

import java.util.*;
import pt.tecnico.distledger.namingserver.ServerEntry;
import pt.tecnico.distledger.namingserver.ServiceEntry;

public class NamingServerState {
    // Crie a classe NamingServer que irá guardar toda a informação que o servidor necessita, 
    // ou seja, contém um mapa que permite associar um nome de um serviço à ServiceEntry correspondente.
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);
    private Map<String, ServiceEntry> servicesMap;

    public NamingServerState(){
        this.servicesMap = new HashMap<String, ServiceEntry>();
    }

    public int addServiceEntry(String serviceName, String qualificator, String serverAddress){
        ServerEntry serverEntry;
        ServiceEntry serviceEntry;
        // if service exists, add serverEntry
        if (servicesMap.containsKey(serviceName)){
            serviceEntry = servicesMap.get(serviceName);
            serverEntry = new ServerEntry(qualificator, serverAddress);
            serviceEntry.addServerEntry(serverEntry);
        }
        // otherwise create service 
        else {
            serverEntry = new ServerEntry(qualificator, serverAddress);
            serviceEntry = new ServiceEntry(serviceName, serverEntry);
            servicesMap.put(serviceName, serviceEntry);
        }
        return 0;
    }

    public void debug(String message) {
        if (DEBUG_FLAG) {
            System.err.println("[DEBUG] " + message);
        }
    }
  
    public void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public List<String> lookup(String serviceName, String qualificator){
        ServiceEntry serviceEntry;
        ServerEntry serverEntry;
        List<ServerEntry> temp;
        List<String> addresses = new ArrayList<String>();
        
        serviceEntry = getServiceEntry(serviceName);
        if (serviceEntry == null){
            return addresses;
        }
        // loop through list of ServerEntrys
        temp = serviceEntry.getServerEntries();
        for (int i = 0; i < temp.size(); i++) {
            serverEntry = temp.get(i);
            if (serverEntry.getQualificator().equals(qualificator)){
                addresses.add(serverEntry.getServerAddress());
            }
        }
        return addresses;
    }

    public int delete(String serviceName, String serverAddress){
        // deletes a server
        ServiceEntry serviceEntry;
        serviceEntry = getServiceEntry(serviceName);
        return serviceEntry.removeServerEntry(serverAddress);
    }  

    public ServiceEntry getServiceEntry(String serviceName){
        return servicesMap.get(serviceName);
    }
}
