package commands;

import java.util.List;
import java.lang.Math;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.ContextException;
import exceptions.NegativeRoot;

public class CommandSqrt extends Command {
	private static final Logger logger = Logger.getLogger(CommandSqrt.class.getName());

	@Override
	public void process(Context context, List<String> args) throws Exception {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandSqrt does not require any arguments");

		if (context.sizeStack() < 1)
			throw new ContextException("CommandSqrt cannot be used: " +
						"stack must contain at list 1 numbers, now it has " +
						context.sizeStack());

		double a = context.pop();
		if (a < 0)
			throw new NegativeRoot("Can not take the root from negative number");
		double res = Math.sqrt(a);
		context.push(res);

		logger.info("CommandSqrt was executed");
	}
}