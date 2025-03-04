package tools;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

import commands.Command;
import exceptions.UnknownCommand;

public class CommandFactory {
	private static final Logger logger = Logger.getLogger(CommandFactory.class.getName());
	private static final Properties knownCommands = new Properties();

	static {
		loadConfig();
	}

	private static void loadConfig() {
		try (InputStream config = CommandFactory.class.getResourceAsStream("/FabricConfig.cfg")) {
			if (config == null)
				throw new IOException("File <FabricConfig.cfg> is not found");
			knownCommands.load(config);
		}
		catch (IOException e){
			logger.severe(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	public static Command getCommand(String name) throws Exception {
		String className = knownCommands.getProperty(name);

		if (className != null)
			return (Command) Class.forName(className).getDeclaredConstructor().newInstance();

		throw new UnknownCommand("Unknown command: '" + name + "'");
	}
}