package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.DivisionByZero;
import exceptions.ContextException;
import exceptions.CalculatorException;

public class CommandDiv extends Command {
	private static final Logger logger = Logger.getLogger(CommandDiv.class.getName());

	@Override
	public void process(Context context, List<String> args) throws CalculatorException {
		if (args.size() > 0)
			logger.warning("unnecessary arguments, CommandDiv does not require any arguments");

		if (context.sizeStack() < 2)
			throw new ContextException("CommandDiv cannot be used: " +
						"stack must contain at list 2 numbers, now it has " +
						context.sizeStack());

		double a = context.pop();
		double b = context.pop();
		if (b == 0)
			throw new DivisionByZero("CommandDiv cannot be used: cannot divide by zero");
		double res = a / b;
		context.push(res);

		logger.info("CommandDiv was executed");
	}
}