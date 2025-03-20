package commands;

import java.util.List;

import tools.Context;
import exceptions.CalculatorException;

public abstract class Command {
	public abstract void process(Context context, List<String> args) throws CalculatorException;
}