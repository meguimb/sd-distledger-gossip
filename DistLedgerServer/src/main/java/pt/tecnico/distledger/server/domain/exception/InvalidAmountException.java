package pt.tecnico.distledger.server.domain.exception;

public class InvalidAmountException extends IllegalArgumentException {
    private final int amount;

    public InvalidAmountException(int amount) {
        super("Invalid amount to transfer: " + amount + ".");
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }
}
