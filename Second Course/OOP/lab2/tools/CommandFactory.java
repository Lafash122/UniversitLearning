package tools;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import commands.Command;
import exceptions.UnknownCommand;
import exceptions.ConfigFileNotFound;
import exceptions.ClassCommandException;
import exceptions.CalculatorException;

public class CommandFactory {
	private static final Logger logger = Logger.getLogger(CommandFactory.class.getName());
	private static final Properties knownCommands = new Properties();

	public CommandFactory() throws ConfigFileNotFound {
		loadConfig();
	}

	private void loadConfig() throws ConfigFileNotFound {
		try (InputStream config = CommandFactory.class.getResourceAsStream("/FabricConfig.cfg")) {
			if (config == null)
				throw new IOException("File <FabricConfig.cfg> is not found");
			knownCommands.load(config);
		}
		catch (IOException i) {
			throw new ConfigFileNotFound(i.getMessage());
		}
	}

	public static Command getCommand(String name) throws CalculatorException {
		String className = knownCommands.getProperty(name);

		if (className != null) {
			try {
				return (Command) Class.forName(className).getDeclaredConstructor().newInstance();
			}
			catch (Exception e) {
				throw new ClassCommandException(e.getMessage());
			}
		}

		throw new UnknownCommand("Unknown command: '" + name + "'");
	}
}