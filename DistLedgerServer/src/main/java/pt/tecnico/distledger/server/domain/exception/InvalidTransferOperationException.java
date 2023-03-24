package pt.tecnico.distledger.server.domain.exception;

public class InvalidTransferOperationException extends IllegalArgumentException{
    public InvalidTransferOperationException() {
        super("Invalid Transfer Operation.");
    }
}
