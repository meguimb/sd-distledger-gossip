package pt.tecnico.distledger.server;

import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateResponse;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import io.grpc.stub.StreamObserver;
import io.grpc.Status;

public class ServerMainAdminServiceImp extends AdminServiceGrpc.AdminServiceImplBase {

    @Override
    public void activate(ActivateRequest request, StreamObserver<ActivateResponse> responseObserver) {
      // TODO
    }

    @Override
    public void deactivate(DeactivateRequest request, StreamObserver<DeactivateResponse> responseObserver) {
      // TODO
    }

    @Override
    public void gossip(GossipRequest request, StreamObserver<GossipResponse> responseObserver) {
      // TODO
    }

    @Override
    public void getLedgerState(getLedgerStateRequest request, StreamObserver<getLedgerStateResponse> responseObserver) {
      // TODO
    }
}
