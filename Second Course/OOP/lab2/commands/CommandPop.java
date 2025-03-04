package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.ContextException;

public class CommandPop extends Command {
	private static final Logger logger = Logger.getLogger(CommandPop.class.getName());

	@Override
	public void process(Context context, List<String> args) throws ContextException {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandDiv does not require any arguments");

		double a = context.pop();

		logger.info("CommandPop was executed");
	}
}