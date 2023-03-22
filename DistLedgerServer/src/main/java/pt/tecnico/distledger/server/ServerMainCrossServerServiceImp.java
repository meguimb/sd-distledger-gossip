package pt.tecnico.distledger.server;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.RegisterResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.DeleteResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import io.grpc.stub.StreamObserver;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import java.util.*;

public class ServerMainCrossServerServiceImp extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {
  // will this server have server properties to propagate between them?
  // what about the constructor?

  private ServerState state;

  public ServerMainCrossServerServiceImp(ServerState s) {
    this.state = s;
  }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
      // ledgerState is a list of Operations
      LedgerState prop_state = request.getState();
      List<DistLedgerCommonDefinitions.Operation> ledger = prop_state.getLedgerList();
      List<Operation> convertedOps = new ArrayList<>();

      // convert list of DistLedgerCommonDefinitions.Operation to domain.Operation
      for(int i = 0; i < ledger.size(); i++) {
        DistLedgerCommonDefinitions.Operation op = ledger.get(i);
        Operation newOp;

        // check anc convert each type of operation
        if (op.getType().equals(OperationType.OP_TRANSFER_TO)){
          newOp = new TransferOp(op.getUserId(), op.getDestUserId(), op.getAmount());
        }
        else if (op.getType().equals(OperationType.OP_CREATE_ACCOUNT)){
          newOp = new CreateOp(op.getUserId());
        }
        else if (op.getType().equals(OperationType.OP_DELETE_ACCOUNT)){
          newOp = new DeleteOp(op.getUserId());
        }
        else {
          newOp = new Operation(op.getUserId());
        }
  
        convertedOps.add(newOp);
      }

      state.propagateState(convertedOps);

      PropagateStateResponse response = PropagateStateResponse.getDefaultInstance();
      state.info("Request to propagate state of server.");
      state.debug("Propagating server's state");

      responseObserver.onNext(response);
      responseObserver.onCompleted();
    }
}