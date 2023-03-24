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

		// create server state
		ServerState serverState = new ServerState(qualificator);

		// create user and admin service implementations for main server
		DistLedgerService distLedgerService = new DistLedgerService(host);
		final BindableService impUser = new ServerMainUserServiceImp(serverState, distLedgerService);
		final BindableService impAdmin = new ServerMainAdminServiceImp(serverState);
		final BindableService impCrossServer = new ServerMainCrossServerServiceImp(serverState);

		distLedgerService.Register(qualificator, String.valueOf(port));

		// create server with all services
		Server server = ServerBuilder.forPort(port).addService(impUser).addService(impAdmin).addService(impCrossServer).build();

		// start server
		server.start();
		System.out.printf("Server %c started at port %d\n", qualificator, port);

		// check for shutdown of server and delete server entries
		System.out.println("Press enter to shutdown");
        System.in.read();
		distLedgerService.Delete(qualificator, String.valueOf(port));

		// close channel and server
		distLedgerService.close();
        server.shutdown();
    }
	
}

