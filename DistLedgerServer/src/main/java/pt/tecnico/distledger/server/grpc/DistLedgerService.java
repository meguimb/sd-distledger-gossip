package pt.tecnico.distledger.server.grpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;

import java.util.ArrayList;
import java.util.List;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import pt.tecnico.distledger.server.domain.exception.SecondaryServerNotActiveException;

public class DistLedgerService {

    public static String target;
    public static String hostname;
    public static ManagedChannel channel;
    static int timestampIndex;
    public static List<Integer> TS;
    static NamingServiceGrpc.NamingServiceBlockingStub stub;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public DistLedgerService(String host) {
        TS = new ArrayList<Integer>();
        TS.add(0);
        TS.add(0);
        TS.add(0);
        // setup host name and port for naming server access
        target = host + ":5001";
        hostname = host;
        // create channel for naming server communication
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

        // create stub
        stub = NamingServiceGrpc.newBlockingStub(channel);
    }

    public List<Integer> getTS() {
        return TS;
    }

    public void updateTS() {
        TS.set(timestampIndex, TS.get(timestampIndex) + 1);
    }
    
    public void Register(char server, String port) {
        try {
            RegisterRequest request = RegisterRequest.newBuilder().setQualificator(String.valueOf(server)).setServerAddress(port).setServiceName("DistLedger").build();
            RegisterResponse response = stub.register(request);
            timestampIndex = response.getTimestampId();
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public LookupResponse Lookup(char server) {
        try {
            LookupRequest request = LookupRequest.newBuilder().setQualificator(String.valueOf(server)).setServiceName("DistLedger").build();
            LookupResponse response = stub.lookup(request);
            return response;
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            return null;
        }
    }

    public void Delete(char server, String port) {
        try {
            DeleteRequest request = DeleteRequest.newBuilder().setServerAddress(port).setServiceName("DistLedger").build();
            DeleteResponse response = stub.delete(request);
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public int PropagateState(LedgerState state) throws SecondaryServerNotActiveException{
        try {
            // call lookup to find host and port for DistLedger service
            LookupRequest lookupRequest = LookupRequest.newBuilder().setQualificator("B").setServiceName("DistLedger").build();
            LookupResponse lookupResponse = stub.lookup(lookupRequest);

            // find and set target to propagate to
            String targetPropagate = hostname + ":" + lookupResponse.getServerAddress(0);

            // setup channel and stub to propagate to
            ManagedChannel channelPropagate = ManagedChannelBuilder.forTarget(targetPropagate).usePlaintext().build();
            DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stubPropagate = DistLedgerCrossServerServiceGrpc.newBlockingStub(channelPropagate);
            
            // call propagate service with created stub
            PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(state).build();
            PropagateStateResponse response = stubPropagate.propagateState(request);

            // close channel
            channelPropagate.shutdown();
            return 0;
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            throw new SecondaryServerNotActiveException();
        }
    }

    public void close() {
        channel.shutdown();
    }
}