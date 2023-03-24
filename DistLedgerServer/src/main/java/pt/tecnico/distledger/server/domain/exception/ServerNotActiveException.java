package pt.tecnico.distledger.server.domain.exception;

public class ServerNotActiveException extends IllegalAccessException {
    public ServerNotActiveException() {
        super("Server is not active");
    }
}
