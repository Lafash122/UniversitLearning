package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;

public class CommandExit extends Command {
	private static final Logger logger = Logger.getLogger(CommandExit.class.getName());

	@Override
	public void process(Context context, List<String> args) {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandExit does not require any arguments");

		context.clear();

		logger.info("programm has been ended by CommandExit");
		System.exit(0);
	}
}