package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.user.UserServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.BalanceResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.CreateAccountResponse;
//import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountRequest;
//import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.DeleteAccountResponse;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToRequest;
import pt.ulisboa.tecnico.distledger.contract.user.UserDistLedger.TransferToResponse;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;

import pt.tecnico.distledger.server.domain.exception.ServerNotActiveException;
import pt.tecnico.distledger.server.domain.exception.SecondaryServerException;
import pt.tecnico.distledger.server.domain.exception.TransferToAndFromSameAccountException;
import pt.tecnico.distledger.server.domain.exception.InvalidAmountException;
import pt.tecnico.distledger.server.domain.exception.AccountDoesNotExistException;
import pt.tecnico.distledger.server.domain.exception.InvalidTransferOperationException;
import pt.tecnico.distledger.server.domain.exception.AccountAlreadyExistsException;

import java.util.*;

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

      // check if id argument is valid
      if (id == null){
        serverState.debug("User id is invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("User id is invalid.").asRuntimeException());
      }
      else{
        serverState.debug("User id is valid.");

        // do balance function if argument of id is valid
        try { 
          value = serverState.balance(id);
          serverState.debug("Checking if server is active and then if account exists...");

          // return value of balance is valid
          serverState.debug("Server is active and account exists.");
          serverState.debug("Returning balance to user.");

          BalanceResponse response = BalanceResponse.newBuilder().setValue(value).build();

          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }

        // check for errors
        catch (ServerNotActiveException e){
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (AccountDoesNotExistException e){
          serverState.debug("Server is active but account doesn't exist.");
          responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (Exception e){
          serverState.debug("Unknown error.");
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

    @Override
    public void createAccount(CreateAccountRequest request, StreamObserver<CreateAccountResponse> responseObserver) {
      String id = request.getUserId();

      serverState.info("Create account request received from user " + id + ".");
      serverState.debug("Checking if user id is valid...");

      // check if id argument is valid
      if (id == null){
        serverState.debug("User id is invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("You can't create an account with this id.").asRuntimeException());
      }
      else {
        serverState.debug("User id is valid.");

        try {
          serverState.debug("Checking if server is active and then if account already exists...");
          // check if server timestamp is higher than the one received
          List<Integer> ts = serverState.getTS();
          CreateAccountResponse response;
          if (ts.get(0) < request.getPrevTS(0) || ts.get(1) < request.getPrevTS(1) || ts.get(2) < request.getPrevTS(2)) {
            serverState.createAddAccount(id, false, ts, request.getPrevTSList(), false, false);
            serverState.debug("Server is active and account doesn't exist.");
            serverState.debug("Creating account for user.");
            serverState.debug("Client has higher timestamp, added unstable op to ledger.");
            response = CreateAccountResponse.newBuilder().addAllTS(request.getPrevTSList()).build();
          }
          else {
            serverState.createAddAccount(id, false, ts, request.getPrevTSList(), true, false);
            serverState.debug("Server is active and account doesn't exist.");
            serverState.debug("Creating account for user.");
            serverState.updateTS();
            response = CreateAccountResponse.newBuilder().addAllTS(serverState.getTS()).build();
          }
          // setup response and propagate to secondary server
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        catch (ServerNotActiveException e) {
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (AccountAlreadyExistsException e) {
          serverState.debug("Server is active but account already exists.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (SecondaryServerException e) {
          serverState.debug("Secondary server cannot perform createAccount operation.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (Exception e) {
          serverState.debug("Unknown error occurred.");
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }

    /*@Override
    public void deleteAccount(DeleteAccountRequest request, StreamObserver<DeleteAccountResponse> responseObserver) {
      String id = request.getUserId();

      serverState.info("Delete account request received from user " + id + ".");
      serverState.debug("Checking if user id is valid...");

      // check for valid id argument in request
      if (id == null){
        serverState.debug("User id is invalid.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to delete account.").asRuntimeException());
      }
      else {
        serverState.debug("User id is valid.");

        // check if we're able to propagate operation to secondary server
        try {
          distLedgerService.PropagateState(makeLedgerState());
        } catch (SecondaryServerNotActiveException e) {
          responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
          return;
        }

        try { 
          serverState.deleteAccount(id, false);
          serverState.debug("Checking if server is active and then if account exists and if it has money...");

          serverState.debug("Server is active, account exists and has no money.");
          serverState.debug("Deleting account.");

          DeleteAccountResponse response = DeleteAccountResponse.getDefaultInstance();
          distLedgerService.PropagateState(makeLedgerState());
          responseObserver.onNext(response);
          responseObserver.onCompleted();
        }
        catch (ServerNotActiveException e) {
          serverState.debug("Server is not active.");
          responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (AccountDoesNotExistException e) {
          serverState.debug("Server is active but account doesn't exist.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (DeleteAccountWithMoneyException e) {
          serverState.debug("Server is active, account exists but has money.");
          responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (SecondaryServerException e) {
          responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
        }
        catch (Exception e) {
          serverState.debug("Unknown error occurred.");
          responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
        }
      }
    }*/

    @Override
    public void transferTo(TransferToRequest request, StreamObserver<TransferToResponse> responseObserver) {
      // get request arguments
      String from = request.getAccountFrom();
      String to = request.getAccountTo();
      int amount = request.getAmount();

      serverState.info("Transfer request received of " + amount + " from " + from + " to " + to + ".");
      
      try {
        serverState.debug("Checking if server is active and then if transfer is valid...");
        // check if server timestamp is higher than the one received
        List<Integer> ts = serverState.getTS();
        TransferToResponse response;

        if (ts.get(0) < request.getPrevTS(0) || ts.get(1) < request.getPrevTS(1) || ts.get(2) < request.getPrevTS(2)) {
          serverState.info("not stable"); //erase
          serverState.transferTo(from, to, amount, false, ts, request.getPrevTSList(), false, false);
          serverState.debug("Server is active.");
          serverState.debug("Client has higher timestamp, added unstable op to ledger.");
          response = TransferToResponse.newBuilder().addAllTS(request.getPrevTSList()).build();
        }
        else {
          serverState.debug("Checking if user ids are valid...");

          // check for invalid arguments in the request
          if (from == null || to == null){
            serverState.debug("User ids are invalid.");
            responseObserver.onError(INVALID_ARGUMENT.withDescription("Invalid id given to transfer.").asRuntimeException());
            return;
          }

        serverState.transferTo(from, to, amount, false, ts, request.getPrevTSList(), true, false);
        serverState.debug("Server is active and transfer is valid.");
        serverState.updateTS();
        response = TransferToResponse.newBuilder().addAllTS(serverState.getTS()).build();
        }
        // set response and propagate with ledgerstate operations
        responseObserver.onNext(response);
        responseObserver.onCompleted();
      } 
      catch (InvalidTransferOperationException e) {
        serverState.debug("Server is active but transfer is invalid, attempted to transfer more money than the account has.");
        responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
      } 
      catch (ServerNotActiveException e) {
        serverState.debug("Server is not active.");
        responseObserver.onError(UNAVAILABLE.withDescription(e.getMessage()).asRuntimeException());
      }
      catch (TransferToAndFromSameAccountException e) {
        serverState.debug("Server is active but transfer is invalid, attempted to transfer to and from the same account.");
        responseObserver.onError(INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
      }
      catch (InvalidAmountException e) {
        serverState.debug("Server is active but transfer is invalid, attempted to transfer an invalid amount.");
        responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException()); 
      }
      catch (AccountDoesNotExistException e) {
        serverState.debug("Server is active but transfer is invalid, attempted to transfer to or from an account that doesn't exist.");
        responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
      }
      catch (SecondaryServerException e) {
        serverState.debug("Secondary server cannot perform transferTo operation.");
        responseObserver.onError(FAILED_PRECONDITION.withDescription(e.getMessage()).asRuntimeException());
      }
      catch (Exception e) {
        serverState.debug("Unknown error occurred.");
        responseObserver.onError(UNKNOWN.withDescription("Unknown error.").asRuntimeException());
      }
    }
}
