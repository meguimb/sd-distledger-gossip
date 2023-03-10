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
      if (id == null){
        responseObserver.onError(INVALID_ARGUMENT.withDescription("User id is invalid.").asRuntimeException());
      }
      else{
        value = serverState.balance(id);
        if (value == -2){
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if (value == -1){
          responseObserver.onError(INVALID_ARGUMENT.withDescription("Account for this user doesn't exist.").asRuntimeException());
        }
        else{
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
      if (id == null){
        responseObserver.onError(INVALID_ARGUMENT.withDescription("You can't create an account with this id.").asRuntimeException());
      }
      else{
        retVal = serverState.createAddAccount(id);
        if (retVal == 0) {
          CreateAccountResponse response = CreateAccountResponse.getDefaultInstance();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        else if(retVal == -2){
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if(retVal == -1){
          responseObserver.onError(FAILED_PRECONDITION.withDescription("Each user can only have one account maximum.").asRuntimeException());
        }
        else{
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
      String id = request.getUserId();
      if (id == null){
        responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to delete account.").asRuntimeException());
      }
      else {
        int result = serverState.deleteAccount(id);
        if (result == 0) {
          DeleteAccountResponse response = DeleteAccountResponse.getDefaultInstance();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        else if (result == -1){
          responseObserver.onError(FAILED_PRECONDITION.withDescription("You can't delete an account that doesn't exist.").asRuntimeException());
        }
        else if(result == -2){
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else if(result == -3) {
          responseObserver.onError(FAILED_PRECONDITION.withDescription("You can't delete an account that has money.").asRuntimeException());
        }
        else{
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
      String from = request.getAccountFrom();
      String to = request.getAccountTo();
      int amount = request.getAmount();
      if (from == null || to == null){
        responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to transfer.").asRuntimeException());
      }
      else{
        int result = serverState.transferTo(from, to, amount);
        if (result == 0) {
          TransferToResponse response = TransferToResponse.getDefaultInstance();
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        else if (result == -3){
          responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid ids to the account. You can't transfer to and from the same account!").asRuntimeException());
        }
        else if (result == -1){
          responseObserver.onError(FAILED_PRECONDITION.withDescription("Invalid Transfer Operation.").asRuntimeException());
        }
        else if(result == -2){
          responseObserver.onError(UNAVAILABLE.withDescription("Server is not active.").asRuntimeException());
        }
        else{
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

}
