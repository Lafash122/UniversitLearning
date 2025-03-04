package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.ContextException;

public class CommandPrint extends Command {
	private static final Logger logger = Logger.getLogger(CommandPrint.class.getName());

	@Override
	public void process(Context context, List<String> args) throws ContextException {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandDiv does not require any arguments");

		System.out.println(context.getNumberStack());

		logger.info("CommandPrint was executed:" + context.getNumberStack());
	}
}