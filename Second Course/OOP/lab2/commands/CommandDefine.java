package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.NotEnoughArguments;
import exceptions.InvalidParameter;
import exceptions.CalculatorException;

public class CommandDefine extends Command {
	private static final Logger logger = Logger.getLogger(CommandDefine.class.getName());

	@Override
	public void process(Context context, List<String> args) throws CalculatorException {
		if (args.size() < 2)
			throw new NotEnoughArguments("to litle arguments, " +
						"CommandDefine requires only 2 arguments: " +
						"Name and Number, but " + args.size() +
						" arguments were received");

		if (args.size() > 2)
			logger.warning("to much arguments, will be used '" +
					args.get(0) + "' '" + args.get(1) + "'");

		if (!args.get(0).matches("\\b[^\\d\\s]+"))
			throw new InvalidParameter("the first argument must not contain a number, " +
						"now it contains '" + args.get(0) + "'");

		double a = Double.parseDouble(args.get(1));
		context.define(args.get(0), a);

		logger.info("CommandDefine was executed");
	}
}