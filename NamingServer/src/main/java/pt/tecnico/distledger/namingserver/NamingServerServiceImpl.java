package pt.tecnico.distledger.namingserver;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServiceGrpc;

import io.grpc.stub.StreamObserver;
import static io.grpc.Status.UNKNOWN;
import java.util.*;

public class NamingServerServiceImpl extends NamingServiceGrpc.NamingServiceImplBase {
    private NamingServerState state;
    public int timestampIndex = 0;

    public NamingServerServiceImpl(NamingServerState state) {
        this.state = state;
    }

    @Override
    public void register(RegisterRequest request, StreamObserver<RegisterResponse> responseObserver) {
        int retVal;
        String serviceName, qualificator, serverAddress;
        serviceName = request.getServiceName();
        qualificator = request.getQualificator();
        serverAddress = request.getServerAddress();

        state.debug("Registering server...");
        retVal = state.addServiceEntry(serviceName, qualificator, serverAddress);

        if (retVal == 0){
            state.info("Registered serverEntry " + serverAddress + " for service " + serviceName + " with qualificator " + qualificator);
            RegisterResponse response = RegisterResponse.newBuilder().setTimestampId(timestampIndex).build();
            timestampIndex++;
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

        state.debug("Lookinp up where this service runs...");
        addresses = state.lookup(serviceName, qualificator);

        state.info("obtained list of servers where this service runs");
        LookupResponse response = LookupResponse.newBuilder().addAllServerAddress(addresses).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void delete(DeleteRequest request, StreamObserver<DeleteResponse> responseObserver){
        String serviceName, serverAddress;

        serviceName = request.getServiceName();
        serverAddress = request.getServerAddress();

        state.debug("Deleting server...");
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
