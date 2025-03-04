package commands;

import java.util.List;

import tools.Context;

public abstract class Command {
	public abstract void process(Context context, List<String> args) throws Exception;
}