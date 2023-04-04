package pt.tecnico.distledger.userclient.grpc;

import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
//import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
//import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class UserService {

    public static String target;
    public static String hostname;
    static NamingServerLookup lookup;
    public static ManagedChannel channel;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public UserService(String host, int port) {
        // setup host and target for user service
        hostname = host;
        target = host + ":" + port;
        lookup = new NamingServerLookup(target);
    }

    public static UserServiceGrpc.UserServiceBlockingStub getStub(String server) {
        // get channel of server using lookup
        channel = ManagedChannelBuilder.forTarget(hostname + ":" + lookup.lookup(server).getServerAddress(0)).usePlaintext().build();
        UserServiceGrpc.UserServiceBlockingStub stub = UserServiceGrpc.newBlockingStub(channel);
        return stub;
    }
    
    public static void createAccount(String server, String username) {
        debug("Attempting to create account for user " + username + "...");
        try {
            // create stub for createAccount
            UserServiceGrpc.UserServiceBlockingStub stub = getStub(server);

            // call callAccount service function
            CreateAccountRequest createAccountRequest = CreateAccountRequest.newBuilder().setUserId(username).build();
            CreateAccountResponse createAccountResponse = stub.createAccount(createAccountRequest);

            // close channel
            channel.shutdown();
            
            System.out.println("OK\n" + createAccountResponse.toString());
            debug("Account created successfully!");
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to create account!");
        }
    }

    /* public static void deleteAccount(String server, String username) {
        debug("Attempting to delete " + username + "'s account...");
        try {
            // create stub for deleteAccount
            UserServiceGrpc.UserServiceBlockingStub stub = getStub(server);
            DeleteAccountRequest deleteAccountRequest = DeleteAccountRequest.newBuilder().setUserId(username).build();
            DeleteAccountResponse deleteAccountResponse = stub.deleteAccount(deleteAccountRequest);

            // close channel of communication
            channel.shutdown();

            System.out.println("OK\n" + deleteAccountResponse.toString());
            debug("Account deleted successfully!");
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to delete account!");
        }
    } */

    public static void balance(String server, String username) {
        debug("Attempting to get " + username + "'s balance...");
        try {
            // get stub to do balance
            UserServiceGrpc.UserServiceBlockingStub stub = getStub(server);

            // call balance service function
            BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUserId(username).build();
            BalanceResponse balanceResponse = stub.balance(balanceRequest);

            // close channel of communication
            channel.shutdown();

            System.out.println("OK\n" + balanceResponse.getValue() + "\n");
            debug("Balance retrieved successfully!");
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to get balance!");
        }
    }

    public static void transferTo(String server, String from, String to, Integer amount) {
        debug("Attempting to transfer " + amount + " from " + from + " to " + to + "...");
        try {
            // get stub to do transferTo
            UserServiceGrpc.UserServiceBlockingStub stub = getStub(server);

            // call transferTo service function
            TransferToRequest transferToRequest = TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(to).setAmount(amount).build();
            TransferToResponse transferToResponse = stub.transferTo(transferToRequest);

            // close channel of communication
            channel.shutdown();

            System.out.println("OK\n" + transferToResponse.toString());
            debug("Transfer successful!");
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to transfer!");
        }
    }

    private static void debug(String message) {
        if (DEBUG_FLAG) {
            System.err.println("[DEBUG] " + message);
        }
    }
}
