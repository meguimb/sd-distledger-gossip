package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.grpc.DistLedgerService;
import pt.tecnico.distledger.server.ServerMainUserServiceImp;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.ServerMainAdminServiceImp;
import java.io.IOException;
import pt.ulisboa.tecnico.distledger.contract.distledgerserver.DistLedgerCrossServerServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ServerMain {
	static String host = "localhost";
	static String target;
    static ManagedChannel channel;
    static DistLedgerCrossServerServiceGrpc.DistLedgerCrossServerServiceBlockingStub blockingStub;

    public static void main(String[] args) throws IOException, InterruptedException {
		// register e lookup do servidor
        // $ mvn exec:java -Dexec.args="2001 A"

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port qualificator\n", ServerMain.class.getSimpleName());
			return;
		}

		// get port and server qualificator
		final int port = Integer.parseInt(args[0]);
		char qualificator = args[1].charAt(0);

		target = host + ":" + port;
		channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        blockingStub = DistLedgerCrossServerServiceGrpc.newBlockingStub(channel);

		// lookup

		// create server state
		ServerState serverState = new ServerState(qualificator);

		// create user and admin service implementations for main server
		final BindableService impUser = new ServerMainUserServiceImp(serverState);
		final BindableService impAdmin = new ServerMainAdminServiceImp(serverState);
		final BindableService impCrossServer = new ServerMainCrossServerServiceImp(serverState);

		DistLedgerService distLedgerService = new DistLedgerService(host);
		distLedgerService.Register(qualificator);

		// create server with both services and start it
		Server server = ServerBuilder.forPort(port).addService(impUser).addService(impAdmin).addService(impCrossServer).build();
		server.start();
		System.out.printf("Server %c started at port %d\n", qualificator, port);
		server.awaitTermination();
    }

	
	public void close() {
        channel.shutdown();
    }
	
}

