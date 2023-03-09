package pt.tecnico.distledger.server;

import pt.tecnico.distledger.server.domain.ServerState;
import pt.tecnico.distledger.server.ServerMainUserServiceImp;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.distledger.server.ServerMainAdminServiceImp;
import java.io.IOException;

public class ServerMain {
    public static void main(String[] args) throws IOException, InterruptedException {

        // $ mvn exec:java -Dexec.args="2001 A"
        System.out.printf("Current Server running is: %s\n", ServerMain.class.getSimpleName());

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port qualificator\n", ServerMain.class.getSimpleName());
			return;
		}

		// get port and ignore qualificator for now
		final int port = Integer.parseInt(args[0]);
		char qualificator = args[1].charAt(0);

		// create server state
		ServerState serverState = new ServerState();

		// create user and admin service implementations for main server
		final BindableService impUser = new ServerMainUserServiceImp(serverState);
		final BindableService impAdmin = new ServerMainAdminServiceImp(serverState);

		// create server with both services and start it
		Server server = ServerBuilder.forPort(port).addService(impUser).addService(impAdmin).build();
		server.start();
		System.out.printf("Server %c started at port %d\n", qualificator, port);
		server.awaitTermination();
    }

}

