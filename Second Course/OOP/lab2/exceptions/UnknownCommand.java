package exceptions;

public class UnknownCommand extends Exception {
	public UnknownCommand(String message) {
		super(message);
	}
}