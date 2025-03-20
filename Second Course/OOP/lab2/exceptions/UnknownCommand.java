package exceptions;

public class UnknownCommand extends FactoryException {
	public UnknownCommand(String message) {
		super(message);
	}
}