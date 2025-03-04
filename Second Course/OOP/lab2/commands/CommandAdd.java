package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.ContextException;

public class CommandAdd extends Command {
	private static final Logger logger = Logger.getLogger(CommandAdd.class.getName());

	@Override
	public void process(Context context, List<String> args) throws ContextException {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandAdd does not require any arguments");

		if (context.sizeStack() < 2)
			throw new ContextException("CommandAdd cannot be used: " +
						"stack must contain at list 2 numbers, now it has " +
						context.sizeStack());

		double a = context.pop();
		double b = context.pop();
		double res = a + b;
		context.push(res);

		logger.info("CommandAdd was executed");
	}
}