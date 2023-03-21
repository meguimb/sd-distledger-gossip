package pt.tecnico.distledger.namingserver;


public class ServerEntry {
    // irá conter a informação para cada servidor, nomeadamente, a combinação host:port e o qualificador. 
    private String qualificator;
    private String serverAddress;

    public ServerEntry(String qualificator, String serverAddress){
        this.qualificator = qualificator;
        this.serverAddress = serverAddress;
    }

    public String getQualificator(){
        return qualificator;
    }

    public String getServerAddress(){
        return serverAddress;
    }
}
