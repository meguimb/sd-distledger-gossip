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
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public AdminService(String host, int port) {
        target = host + ":" + port;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        blockingStub = AdminServiceGrpc.newBlockingStub(channel);
    }

    public static void activate(String server) {
        debug("Attempting to activate server");
        try {
            ActivateRequest activateRequest = ActivateRequest.getDefaultInstance();
            ActivateResponse activateResponse = blockingStub.activate(activateRequest);
            System.out.println("OK\n" + activateResponse.toString());
            debug("Server activated successfully!");
        }
        catch(StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to activate server!");
        }
    }

    public static void deactivate(String server) {
        debug("Attempting to deactivate server");
        try {
            DeactivateRequest deactivateRequest = DeactivateRequest.getDefaultInstance();
            DeactivateResponse activateResponse = blockingStub.deactivate(deactivateRequest);
            System.out.println("OK\n" + activateResponse.toString());
            debug("Server deactivated successfully!");
        }
        catch(StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to deactivate server!");
        }
    }

    public static void getLedgerState(String server) {
        debug("Attempting to get ledger state");
        try {
            getLedgerStateRequest request = getLedgerStateRequest.getDefaultInstance();
            getLedgerStateResponse response = blockingStub.getLedgerState(request);
            System.out.println("OK\n" + response.toString());
            debug("Ledger state retrieved successfully!");
        }
        catch(StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to get ledger state!");
        }
    }

    private static void debug(String message) {
        if (DEBUG_FLAG) {
            System.err.println("[DEBUG] " + message);
        }
    }

    public void close() {
        channel.shutdown();
    }
}
