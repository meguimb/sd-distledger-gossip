package pt.tecnico.distledger.server;
public class ServerMain {

    public static void main(String[] args) {

        // Nesta fase o serviço é prestado por um único servidor, que aceita 
        // pedidos num endereço/porto fixo que é conhecido de antemão por todos os clientes.

        // Cada servidor exporta múltiplas interfaces. Cada interface está pensada para 
        // expor operações a cada tipo de cliente (utilizadores e administradores). Para 
        // além dessas, os servidores exportam uma terceira interface pensada para ser 
        //invocada por outros servidores (no caso em que os servidores estão replicados, 
        // e necessitam de comunicar entre si).

        // O servidor deve ser lançado a partir da pasta Server, recebendo como argumentos
        // o porto e o seu qualificador ('A', 'B', etc.). Na fase 1, o qualificador passado 
        // é ignorado.

        // Por exemplo, um servidor primário pode ser lançado da seguinte forma a partir da
        // pasta Server ($ representa a shell do sistema operativo):

        // $ mvn exec:java -Dexec.args="2001 A"
        System.out.printf("Current Server running is: %s", ServerMain.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s port qualificator\n", ServerMain.class.getSimpleName());
			return;
		}

		final int port = Integer.parseInt(args[0]);
        /* 
		final BindableService impl = new TTTServiceImpl();

		// Create a new server to listen on port
		Server server = ServerBuilder.forPort(port).addService(impl).build();

		// Start the server
		server.start();

		// Server threads are running in the background.
		System.out.println("Server started");

		// Do not exit the main thread. Wait until server is terminated.
		server.awaitTermination();
        */
    }

}

