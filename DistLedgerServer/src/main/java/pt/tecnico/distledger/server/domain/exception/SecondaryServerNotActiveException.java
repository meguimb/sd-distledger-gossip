package pt.tecnico.distledger.server.domain.exception;

public class SecondaryServerNotActiveException extends IllegalAccessException {
    public SecondaryServerNotActiveException() {
        super("Secondary server is deactivated.");
    }
}
