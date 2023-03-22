package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.UNAVAILABLE;
import static io.grpc.Status.FAILED_PRECONDITION;
import static io.grpc.Status.UNKNOWN;

public class ServerMainUserServiceImp extends UserServiceGrpc.UserServiceImplBase {
  private ServerState serverState;

  public ServerMainUserServiceImp(ServerState s) {
    this.serverState = s;
  }

    @Override
    public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
      String id; int value;

      id = request.getUserId();

      serverState.info("Balance request received from user " + id + ".");
      serverState.debug("Checking if user id is valid...");

      if (id == null){
        serverState.debug("User id is invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("User id is invalid.").asRuntimeException());
      }
      else{
        serverState.debug("User id is valid.");
        value = serverState.balance(id);
        serverState.debug("Checking if server is active and then if account exists...");

        if (value == -2){
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if (value == -1){
          serverState.debug("Server is active but account doesn't exist.");
          responseObserver.onError(INVALID_ARGUMENT.withDescription("Account for this user doesn't exist.").asRuntimeException());
        }
        else{
          serverState.debug("Server is active and account exists.");
          serverState.debug("Returning balance to user.");

          BalanceResponse response = BalanceResponse.newBuilder().setValue(value).build();

          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
      }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
      int retVal;

      String id = request.getUserId();

      serverState.info("Create account request received from user " + id + ".");
      serverState.debug("Checking if user id is valid...");

      if (id == null){
        serverState.debug("User id is invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("You can't create an account with this id.").asRuntimeException());
      }
      else{
        serverState.debug("User id is valid.");
        retVal = serverState.createAddAccount(id);
        serverState.debug("Checking if server is active and then if account already exists...");

        if (retVal == 0) {
          serverState.debug("Server is active and account doesn't exist.");
          serverState.debug("Creating account for user.");

          CreateAccountResponse response = CreateAccountResponse.getDefaultInstance();

          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        else if(retVal == -2){
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if(retVal == -1){
          serverState.debug("Server is active but account already exists.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription("Each user can only have one account maximum.").asRuntimeException());
        }
        else if(retVal == -3) {
          responseObserver.onError(FAILED_PRECONDITION.withDescription("This server is secondary").asRuntimeException());
        }
        else{
          serverState.debug("Unknown error occurred.");
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
      String id = request.getUserId();

      serverState.info("Delete account request received from user " + id + ".");
      serverState.debug("Checking if user id is valid...");

      if (id == null){
        serverState.debug("User id is invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to delete account.").asRuntimeException());
      }
      else {
        serverState.debug("User id is valid.");
        int result = serverState.deleteAccount(id);
        serverState.debug("Checking if server is active and then if account exists and if it has money...");

        if (result == 0) {
          serverState.debug("Server is active, account exists and has no money.");
          serverState.debug("Deleting account.");

          DeleteAccountResponse response = DeleteAccountResponse.getDefaultInstance();

          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        else if(result == -2){
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if (result == -1){
          serverState.debug("Server is active but account doesn't exist.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription("You can't delete an account that doesn't exist.").asRuntimeException());
        }
        else if(result == -3) {
          serverState.debug("Server is active, account exists but has money.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription("You can't delete an account that has money.").asRuntimeException());
        }
        else if(result == -4) {
          responseObserver.onError(FAILED_PRECONDITION.withDescription("This server is secondary").asRuntimeException());
        }
        else{
          serverState.debug("Unknown error occurred.");
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
      String from = request.getAccountFrom();
      String to = request.getAccountTo();
      int amount = request.getAmount();

      serverState.info("Transfer request received of " + amount + " from " + from + " to " + to + ".");
      serverState.debug("Checking if user ids are valid...");

      if (from == null || to == null){
        serverState.debug("User ids are invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to transfer.").asRuntimeException());
      }
      else{
        serverState.debug("User ids are valid.");
        int result = serverState.transferTo(from, to, amount);
        serverState.debug("Checking if server is active and then if transfer is valid...");

        if (result == 0) {
          serverState.debug("Server is active and transfer is valid.");
          serverState.debug("Transferring money.");
          
          TransferToResponse response = TransferToResponse.getDefaultInstance();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        else if (result == -1){
          serverState.debug("Server is active but transfer is invalid, attempted to transfer more money than the account has.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription("Invalid Transfer Operation.").asRuntimeException());
        }
        else if(result == -2){
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if (result == -3){
          serverState.debug("Server is active but transfer is invalid, attempted to transfer to and from the same account.");
          responseObserver.onError(INVALID_ARGUMENT.withDescription("You can't transfer to and from the same account!").asRuntimeException());
        }
        else if (result == -4){
          serverState.debug("Server is active but transfer is invalid, attempted to transfer an invalid amount.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription("Invalid amount to transfer.").asRuntimeException());
        }
        else if (result == -5){
          serverState.debug("Server is active but one or both accounts don't exist.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription("Accounts don't exist.").asRuntimeException());
        }
        else if(result == -6) {
          responseObserver.onError(FAILED_PRECONDITION.withDescription("This server is secondary").asRuntimeException());
        }
        else{
          serverState.debug("Unknown error occurred.");
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }
}
