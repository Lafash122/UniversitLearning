package exceptions;

public class InvalidParameter extends OperationException {
	public InvalidParameter(String message) {
		super(message);
	}
}