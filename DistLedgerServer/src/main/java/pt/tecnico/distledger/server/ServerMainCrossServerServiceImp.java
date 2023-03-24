package pt.tecnico.distledger.server;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
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

import pt.tecnico.distledger.server.domain.exception.SecondaryServerNotActiveException;

public class ServerMainCrossServerServiceImp extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

  private ServerState state;

  public ServerMainCrossServerServiceImp(ServerState s) {
    this.state = s;
  }

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
      // get llist of ledger operations
      LedgerState prop_state = request.getState();
      List<DistLedgerCommonDefinitions.Operation> ledger = prop_state.getLedgerList();
      List<Operation> convertedOps = new ArrayList<>();

      // convert list of DistLedgerCommonDefinitions.Operation to domain.Operation
      for(int i = 0; i < ledger.size(); i++) {
        DistLedgerCommonDefinitions.Operation op = ledger.get(i);
        Operation newOp;

        // check and convert each type of operation
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
      state.info("Request to propagate state of server.");

      // do propagation and check for errors
      try {
        int result = state.propagateState(convertedOps);

        state.info("State propagated successfully");
        PropagateStateResponse response = PropagateStateResponse.getDefaultInstance();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
      } catch (Exception e) {
        state.debug("Cannot perform propagation when secondary server is deactivated.");
        responseObserver.onError(new RuntimeException("Secondary server is deactivated"));
      }
    }
}