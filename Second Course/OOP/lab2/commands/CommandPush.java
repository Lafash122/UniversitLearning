package commands;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import tools.Context;
import exceptions.NotEnoughArguments;
import exceptions.ContextException;

public class CommandPush extends Command {
	private static final Logger logger = Logger.getLogger(CommandPush.class.getName());

	@Override
	public void process(Context context, List<String> args) throws Exception {
		if (args.size() < 1)
			throw new NotEnoughArguments("to litle arguments, " +
						"CommandPush requires only 1 arguments: " +
						"Name or Number, but " + args.size() +
						" arguments were received");
		else {
			if (args.size() > 1)
				logger.warning("to much arguments, will be used '" + args.get(0) + "'");
			try {
				double a = Double.parseDouble(args.get(0));
				context.push(a);
			}
			catch (Exception e) {
				if (!context.hasParam(args.get(0)))
					throw new ContextException("list of defined parameters " +
								"does not contain such parameter");

				double a = context.getNumberMap(args.get(0));
				context.push(a);
			}
		}

		logger.info("CommandPush was executed");
	}
}