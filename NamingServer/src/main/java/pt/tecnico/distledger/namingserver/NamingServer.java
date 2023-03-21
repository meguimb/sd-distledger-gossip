package pt.tecnico.distledger.namingserver;

import java.util.*;
import pt.tecnico.distledger.namingserver.NamingServerServiceImpl;
import pt.tecnico.distledger.namingserver.ServerEntry;
import pt.tecnico.distledger.namingserver.ServiceEntry;
import pt.tecnico.distledger.namingserver.NamingServerState;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;

public class NamingServer {
    // irá guardar toda a informação que o servidor necessita, ou seja, contém 
    // um mapa que permite associar um nome de um serviço à ServiceEntry correspondente.

    Integer port = 5001;

    public void main(String[] args) throws IOException, InterruptedException{

        // $ mvn exec:java -Dexec.args="5001 A"
        System.out.printf("Current Server running is: %s\n", NamingServer.class.getSimpleName());

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port qualificator\n", NamingServer.class.getSimpleName());
			return;
		}

		// get port and ignore qualificator for now

        // create server state
		NamingServerState state = new NamingServerState();

		// create user and admin service implementations for main server
		final BindableService impNamingServer = new NamingServerServiceImpl(state);

		// create server with both services and start it
		Server server = ServerBuilder.forPort(port).addService(impNamingServer).build();
		server.start();
		System.out.printf("Naming Server started at port %d\n", port);
		server.awaitTermination();

    }

}
