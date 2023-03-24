package pt.tecnico.distledger.server.grpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServiceGrpc.NamingServiceImplBase;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServiceGrpc;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import java.util.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class DistLedgerService {

    public static String target;
    public static String hostname;
    public static ManagedChannel channel;
    static NamingServiceGrpc.NamingServiceBlockingStub stub;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public DistLedgerService(String host) {
        target = host + ":5001";
        hostname = host;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = NamingServiceGrpc.newBlockingStub(channel);
    }
    
    public void Register(char server, String port) {
        try {
            RegisterRequest request = RegisterRequest.newBuilder().setQualificator(String.valueOf(server)).setServerAddress(port).setServiceName("DistLedger").build();
            RegisterResponse response = stub.register(request);
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

    public int PropagateState(LedgerState state) {
        try {
            LookupRequest lookupRequest = LookupRequest.newBuilder().setQualificator("B").setServiceName("DistLedger").build();
            LookupResponse lookupResponse = stub.lookup(lookupRequest);
            String targetPropagate = hostname + ":" + lookupResponse.getServerAddress(0);
            ManagedChannel channelPropagate = ManagedChannelBuilder.forTarget(targetPropagate).usePlaintext().build();
            DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub stubPropagate = DistLedgerCrossServerServiceGrpc.newBlockingStub(channelPropagate);
            PropagateStateRequest request = PropagateStateRequest.newBuilder().setState(state).build();
            PropagateStateResponse response = stubPropagate.propagateState(request);
            channelPropagate.shutdown();
            return 0;
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            return -1;
        }
    }

    public void close() {
        channel.shutdown();
    }
}