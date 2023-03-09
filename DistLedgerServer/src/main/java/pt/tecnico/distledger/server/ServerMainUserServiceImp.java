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
import pt.tecnico.distledger.server.domain.Account;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.ServerMain;
import pt.tecnico.distledger.server.domain.ServerState;
import static io.grpc.Status.INVALID_ARGUMENT;
import io.grpc.Status;

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
        if (value == -1){
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
      String id = request.getUserId();
      if (id == null){
        responseObserver.onError(INVALID_ARGUMENT.withDescription("You can't create an account with this id.").asRuntimeException());
      }
      else{
        serverState.createAddAccount(id);
        CreateAccountResponse response = CreateAccountResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    }

    @Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
      String id = request.getUserId();
      if (id == null){
        responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to delete account.").asRuntimeException());
      }
      else{
        if (serverState.deleteAccount(id) == -1){
          responseObserver.onError(INVALID_ARGUMENT.withDescription("You can't delete an account that doesn't exist.").asRuntimeException());
        }
        DeleteAccountResponse response = DeleteAccountResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
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
        if (serverState.transferTo(from, to, amount) == -1){
          responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid Operation.").asRuntimeException());
        }
        TransferToResponse response = TransferToResponse.getDefaultInstance();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      }
    }

}
