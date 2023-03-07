package pt.tecnico.distledger.server;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import io.grpc.stub.StreamObserver;

public class ServerMainCrossServerServiceImp extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private ServerMain server = new ServerMain();

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
      // TODO

    }
}