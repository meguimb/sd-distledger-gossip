package pt.tecnico.distledger.userclient.grpc;

import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

public class UserService {

    final String target;
    final ManagedChannel channel;
    static UserServiceGrpc.UserServiceBlockingStub stub;
    private static final boolean DEBUG_FLAG = (System.getProperty("debug") != null);

    public UserService(String host, int port) {
        target = host + ":" + port;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = UserServiceGrpc.newBlockingStub(channel);
    }
    
    public static void createAccount(String server, String username) {
        debug("Attempting to create account for user " + username + "...");
        try {
            CreateAccountRequest createAccountRequest = CreateAccountRequest.newBuilder().setUserId(username).build();
            CreateAccountResponse createAccountResponse = stub.createAccount(createAccountRequest);
            System.out.println("OK\n" + createAccountResponse.toString());
            debug("Account created successfully!");
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to create account!");
        }
    }

    public static void deleteAccount(String server, String username) {
        debug("Attempting to delete " + username + "'s account...");
        try {
            DeleteAccountRequest deleteAccountRequest = DeleteAccountRequest.newBuilder().setUserId(username).build();
            DeleteAccountResponse deleteAccountResponse = stub.deleteAccount(deleteAccountRequest);
            System.out.println("OK\n" + deleteAccountResponse.toString());
            debug("Account deleted successfully!");
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
            debug("Failed to delete account!");
        }
    }

    public static void balance(String server, String username) {
        debug("Attempting to get " + username + "'s balance...");
        try {
            BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUserId(username).build();
            BalanceResponse balanceResponse = stub.balance(balanceRequest);
            System.out.println("OK\n" + balanceResponse.toString());
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
            TransferToRequest transferToRequest = TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(to).setAmount(amount).build();
            TransferToResponse transferToResponse = stub.transferTo(transferToRequest);
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
            System.err.println(message);
        }
    }

    public void close() {
        channel.shutdown();
    }
}
