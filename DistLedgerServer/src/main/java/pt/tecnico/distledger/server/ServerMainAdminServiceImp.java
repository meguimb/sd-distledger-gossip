package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;
import pt.tecnico.distledger.server.domain.ServerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.tecnico.distledger.server.domain.operation.Operation;
import pt.tecnico.distledger.server.domain.operation.CreateOp;
import pt.tecnico.distledger.server.domain.operation.DeleteOp;
import pt.tecnico.distledger.server.domain.operation.TransferOp;
import io.grpc.stub.StreamObserver;
import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.OperationType;
import io.grpc.Status;

import java.util.*;

public class ServerMainAdminServiceImp extends AdminServiceGrpc.AdminServiceImplBase {
  private ServerState state;

  public ServerMainAdminServiceImp(ServerState s) {
    this.state = s;
  }

  @Override
  public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
    ActivateResponse response = ActivateResponse.getDefaultInstance();
    state.info("Request to activate server received from admin");
    state.debug("Activating server");
    state.activate();

    responseObserver.onNext(response);
	  responseObserver.onCompleted();
  }

  @Override
  public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
    DeactivateResponse response = DeactivateResponse.getDefaultInstance();
    state.info("Request to deactivate server received from admin");
    state.debug("Deactivating server");
    state.deactivate();

    responseObserver.onNext(response);
	  responseObserver.onCompleted();
  }

  @Override
  public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
    // TODO - fase 3
  }

  @Override
  public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
    List<Operation> ledgerOps = state.getLedgerState();
    List<DistLedgerCommonDefinitions.Operation> convertedOps = new ArrayList<>();
    state.info("Request to get ledger state received from admin");
    state.debug("Converting ledger state to protobuf format...");

    for(int i = 0; i < ledgerOps.size(); i++) {
      Operation op = ledgerOps.get(i);
      DistLedgerCommonDefinitions.Operation newOp;

      // check each type of operation
      if(op instanceof TransferOp) {
        TransferOp transferOp = (TransferOp) op;
        newOp = DistLedgerCommonDefinitions.Operation.newBuilder().setType(OperationType.OP_TRANSFER_TO).setUserId(transferOp.getAccount()).setDestUserId(transferOp.getDestAccount()).setAmount(transferOp.getAmount()).build();
      }
      else if(op instanceof CreateOp) {
        CreateOp createOp = (CreateOp) op;
        newOp = DistLedgerCommonDefinitions.Operation.newBuilder().setType(OperationType.OP_CREATE_ACCOUNT).setUserId(createOp.getAccount()).build();
      }
      else if(op instanceof DeleteOp) {
        DeleteOp deleteOp = (DeleteOp) op;
        newOp = DistLedgerCommonDefinitions.Operation.newBuilder().setType(OperationType.OP_DELETE_ACCOUNT).setUserId(deleteOp.getAccount()).build();
      }
      else {
        newOp = DistLedgerCommonDefinitions.Operation.newBuilder().setType(OperationType.OP_UNSPECIFIED).build();
      }

      convertedOps.add(newOp);
    }
    state.debug("Returning ledger state to admin");
    DistLedgerCommonDefinitions.LedgerState ledgerState = DistLedgerCommonDefinitions.LedgerState.newBuilder().addAllLedger(convertedOps).build();
    getLedgerStateResponse response = getLedgerStateResponse.newBuilder().setLedgerState(ledgerState).build();

    responseObserver.onNext(response);
	  responseObserver.onCompleted();
  }
}
