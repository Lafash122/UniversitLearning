package exceptions;

public class NotEnoughArguments extends OperationException {
	public NotEnoughArguments(String message) {
		super(message);
	}
}