package pt.tecnico.distledger.adminclient.grpc;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.function.Function;

public class AdminService {

    final String target;
    final ManagedChannel channel;
    static AdminServiceGrpc.AdminServiceBlockingStub blockingStub;

    public AdminService(String host, int port) {
        target = host + ":" + port;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        blockingStub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public static void activate(String server) {
        try {
            ActivateRequest activateRequest = ActivateRequest.getDefaultInstance();
            ActivateResponse activateResponse = blockingStub.activate(activateRequest);
            System.out.println("OK\n" + activateResponse.toString());
        }
        catch(StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public static void deactivate(String server) {
        try {
            DeactivateRequest deactivateRequest = DeactivateRequest.getDefaultInstance();
            DeactivateResponse activateResponse = blockingStub.deactivate(deactivateRequest);
            System.out.println("OK\n" + activateResponse.toString());
        }
        catch(StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public static void getLedgerState(String server) {
        try {
            getLedgerStateRequest request = getLedgerStateRequest.getDefaultInstance();
            getLedgerStateResponse response = blockingStub.getLedgerState(request);
            System.out.println("OK\n" + response.toString());
        }
        catch(StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public void close() {
        channel.shutdown();
    }
}
