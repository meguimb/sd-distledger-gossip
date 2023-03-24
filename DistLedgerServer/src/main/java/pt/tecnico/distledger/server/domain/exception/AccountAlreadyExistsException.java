package pt.tecnico.distledger.server.domain.exception;

public class AccountAlreadyExistsException extends IllegalArgumentException {
    public AccountAlreadyExistsException() {
        super("Each user can only have one account maximum.");
    }
}
