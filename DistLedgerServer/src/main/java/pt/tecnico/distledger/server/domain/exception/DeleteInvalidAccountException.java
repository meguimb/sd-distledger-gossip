package pt.tecnico.distledger.server.domain.exception;

public class DeleteInvalidAccountException extends IllegalArgumentException {
    public DeleteInvalidAccountException() {
        super("You can't delete an account that doesn't exist.");
    }
}
