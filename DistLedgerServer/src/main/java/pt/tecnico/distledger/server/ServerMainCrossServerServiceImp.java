package pt.tecnico.distledger.server;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateRequest;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.CrossServerDistLedger.PropagateStateResponse;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import io.grpc.stub.StreamObserver;

import io.grpc.Status;

public class ServerMainCrossServerServiceImp extends DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceImplBase {

    private ServerMain server = new ServerMain();

    @Override
    public void propagateState(PropagateStateRequest request, StreamObserver<PropagateStateResponse> responseObserver) {
      // TODO

    }
}
	/** Game implementation. */
    /*
	private TTTGame ttt = new TTTGame();

	@Override
	public void currentBoard(CurrentBoardRequest request, StreamObserver<CurrentBoardResponse> responseObserver) {
		// StreamObserver is used to represent the gRPC stream between the server and
		// client in order to send the appropriate responses (or errors, if any occur).

		CurrentBoardResponse response = CurrentBoardResponse.newBuilder().setBoard(ttt.toString()).build();

		// Send a single response through the stream.
		responseObserver.onNext(response);
		// Notify the client that the operation has been completed.
		responseObserver.onCompleted();
	}
	
	@Override
	public void play(PlayRequest request, StreamObserver<PlayResponse> responseObserver){
		int row, column, player;

		row = request.getRow();
		column = request.getColumn();
		player = request.getPlayer();

		PlayResult playResult = ttt.play(row, column, player);

		// error checking
		if (playResult == PlayResult.OUT_OF_BOUNDS){
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Jogada Inválida!").asRuntimeException());
		}
		else if(playResult == PlayResult.UNKNOWN){
			responseObserver.onError(INVALID_ARGUMENT.withDescription("Input has to be known").asRuntimeException());
		}	
		else if(playResult == PlayResult.SQUARE_TAKEN){
			responseObserver.onError(INVALID_ARGUMENT.withDescription("This square is already taken").asRuntimeException());
		}
		else{
			// Send a single response through the stream.
			PlayResponse response = PlayResponse.newBuilder().setResult(playResult).build();
			responseObserver.onNext(response);
			// Notify the client that the operation has been completed.
			responseObserver.onCompleted();
		} 
	}

	@Override
	public void checkWinner(CheckWinnerRequest request, StreamObserver<CheckWinnerResponse> responseObserver){

		int winner = ttt.checkWinner();
		CheckWinnerResponse response = CheckWinnerResponse.newBuilder().setWinner(winner).build();

		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
    */