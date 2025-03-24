package exceptions;

public class ConfigFileNotFound extends FactoryException {
	public ConfigFileNotFound(String message) {
		super(message);
	}
}