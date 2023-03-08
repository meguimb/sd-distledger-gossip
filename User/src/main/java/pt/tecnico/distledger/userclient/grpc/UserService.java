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

    public UserService(String host, int port) {
        target = host + ":" + port;
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        stub = UserServiceGrpc.newBlockingStub(channel);
    }
    
    public static void createAccount(String server, String username) {
        try {
            CreateAccountRequest createAccountRequest = CreateAccountRequest.newBuilder().setUserId(username).build();
            CreateAccountResponse createAccountResponse = stub.createAccount(createAccountRequest);
            System.out.println("OK\n" + createAccountResponse.toString());
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public static void deleteAccount(String server, String username) {
        try {
            DeleteAccountRequest deleteAccountRequest = DeleteAccountRequest.newBuilder().setUserId(username).build();
            DeleteAccountResponse deleteAccountResponse = stub.deleteAccount(deleteAccountRequest);
            System.out.println("OK\n" + deleteAccountResponse.toString());
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public static void balance(String server, String username) {
        try {
            BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUserId(username).build();
            BalanceResponse balanceResponse = stub.balance(balanceRequest);
            System.out.println("OK\n" + balanceResponse.toString());
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }

    public static void transferTo(String server, String from, String to, Integer amount) {
        try {
            TransferToRequest transferToRequest = TransferToRequest.newBuilder().setAccountFrom(from).setAccountTo(to).setAmount(amount).build();
            TransferToResponse transferToResponse = stub.transferTo(transferToRequest);
            System.out.println("OK\n" + transferToResponse.toString());
        }
        catch (StatusRuntimeException e) {
            System.out.println("ERROR\n" + e.getStatus().getDescription());
        }
    }


}
