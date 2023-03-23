package pt.tecnico.distledger.adminclient.grpc;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class NamingServerLookup {

    final ManagedChannel channel;
    static NamingServiceGrpc.NamingServiceBlockingStub stub;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public NamingServerLookup(String target) {
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = NamingServiceGrpc.newBlockingStub(channel);
    }
    
    public LookupResponse lookup(String server) {
        LookupRequest lookupRequest = LookupRequest.newBuilder().setQualificator(server).setServiceName("DistLedger").build();
        LookupResponse lookupResponse = stub.lookup(lookupRequest);
        return lookupResponse;
    }

    public void close() {
        channel.shutdown();
    }
}
