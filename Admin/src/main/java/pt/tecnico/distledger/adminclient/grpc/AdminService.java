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

    public static String target;
    public static String hostname;
    static NamingServerLookup lookup;
    public static ManagedChannel channel;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public AdminService(String host, int port) {
        hostname = host;
        target = host + ":" + port;
        lookup = new NamingServerLookup(target);
    }

    public static AdminServiceGrpc.AdminServiceBlockingStub getStub(String server) {
        channel = ManagedChannelBuilder.forTarget(hostname + ":" + lookup.lookup(server).getServerAddress(0)).usePlaintext().build();
        AdminServiceGrpc.AdminServiceBlockingStub stub = AdminServiceGrpc.newBlockingStub(channel);
        return stub;
    }

    public static void activate(String server) {
        debug("Attempting to activate server");
        try {
            AdminServiceGrpc.AdminServiceBlockingStub stub = getStub(server);
            ActivateRequest activateRequest = ActivateRequest.getDefaultInstance();
            ActivateResponse activateResponse = stub.activate(activateRequest);
            channel.shutdown();
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
            AdminServiceGrpc.AdminServiceBlockingStub stub = getStub(server);
            DeactivateRequest deactivateRequest = DeactivateRequest.getDefaultInstance();
            DeactivateResponse activateResponse = stub.deactivate(deactivateRequest);
            channel.shutdown();
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
            AdminServiceGrpc.AdminServiceBlockingStub stub = getStub(server);
            getLedgerStateRequest request = getLedgerStateRequest.getDefaultInstance();
            getLedgerStateResponse response = stub.getLedgerState(request);
            System.out.println("OK\n" + response.toString());
            channel.shutdown();
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
