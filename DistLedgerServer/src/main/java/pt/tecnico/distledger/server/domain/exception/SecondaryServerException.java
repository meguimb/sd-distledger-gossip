package pt.tecnico.distledger.server.domain.exception;

public class SecondaryServerException extends IllegalAccessException {
    public SecondaryServerException() {
        super("This server is secondary");
    }
}
