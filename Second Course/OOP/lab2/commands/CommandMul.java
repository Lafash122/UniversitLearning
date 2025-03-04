package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.ContextException;

public class CommandMul extends Command {
	private static final Logger logger = Logger.getLogger(CommandMul.class.getName());

	@Override
	public void process(Context context, List<String> args) throws ContextException {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandMul does not require any arguments");

		if (context.sizeStack() < 2)
			throw new ContextException("CommandMul cannot be used: " +
						"stack must contain at list 2 numbers, now it has " +
						context.sizeStack());

		double a = context.pop();
		double b = context.pop();
		double res = a * b;
		context.push(res);

		logger.info("CommandMul was executed");
	}
}