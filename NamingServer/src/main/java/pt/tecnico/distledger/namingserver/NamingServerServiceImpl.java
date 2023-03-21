package pt.tecnico.distledger.namingserver;

import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import pt.tecnico.distledger.namingserver.ServerEntry;
import pt.tecnico.distledger.namingserver.ServiceEntry;
import pt.tecnico.distledger.namingserver.NamingServer;
import pt.tecnico.distledger.namingserver.NamingServerState;

import io.grpc.stub.StreamObserver;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;
import static io.grpc.Status.FAILED_PRECONDITION;
import static io.grpc.Status.UNKNOWN;
import java.util.*;

public class NamingServerServiceImpl extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {
    private NamingServerState state;

    public NamingServerServiceImpl(NamingServerState state) {
        this.state = state;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        int retVal;
        String serviceName, qualificator, serverAddress;
        ServerEntry serverEntry;
        ServiceEntry serviceEntry;

        serviceName = request.getServiceName();
        qualificator = request.getQualificator();
        serverAddress = request.getServerAddress();

        retVal = state.addServiceEntry(serviceName, qualificator, serverAddress);

        if (retVal == 0){
            state.info("Registered service %s at server %s at %s.");
            RegisterResponse response = RegisterResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else if(retVal == -1){
            responseObserver.onError(UNKNOWN.withDescription("Not possible to register the server").asRuntimeException());
        }
    }

    @Override
    public void lookup(LookupRequest request, StreamObserver<LookupResponse> responseObserver) {
        String serviceName, qualificator;
        List<String> addresses = new ArrayList<String>();

        serviceName = request.getServiceName();
        qualificator = request.getQualificator();
        addresses = state.lookup(serviceName, qualificator);

        state.info("obtained list of servers where this service runs");
        LookupResponse response = LookupResponse.newBuilder().addAllServerAddress(addresses).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver){
        int retVal;
        String serviceName, serverAddress;

        serviceName = request.getServiceName();
        serverAddress = request.getServerAddress();

        if (state.delete(serviceName, serverAddress) == 0){
            DeleteResponse response = DeleteResponse.getDefaultInstance();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else {
            responseObserver.onError(UNKNOWN.withDescription("Not possible to remove the server").asRuntimeException());
        }
    }
}
