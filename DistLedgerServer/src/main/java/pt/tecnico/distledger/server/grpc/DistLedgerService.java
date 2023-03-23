package pt.tecnico.distledger.server.grpc;
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
    public static ManagedChannel channel;
    static NamingServiceGrpc.NamingServiceBlockingStub stub;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public DistLedgerService(String host) {
        target = host + ":5001";
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = NamingServiceGrpc.newBlockingStub(channel);
    }
    
    public void Register(char server) {
        try {
            RegisterRequest request = RegisterRequest.newBuilder().setQualificator(String.valueOf(server)).setServerAddress("2001").setServiceName("DistLedger").build();
            RegisterResponse response = stub.register(request);
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }
}