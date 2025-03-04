package tools;

import java.util.Map;
import java.util.HashMap;
import java.util.Deque;
import java.util.ArrayDeque;

import exceptions.ContextException;

public class Context {
	private Map<String, Double> definedParams;
	private Deque<Double> stack;

	public Context() {
		definedParams = new HashMap<>();
		stack = new ArrayDeque<>();
	}

	public int sizeStack() {
		return stack.size();
	}

	public void push(double number) {
		stack.push(number);
	}

	public double pop() throws ContextException {
		if (!stack.isEmpty())
			return stack.pop();
		throw new ContextException("<pop> cannot be executed: stack is empty");
	}

	public double getNumberStack() throws ContextException {
		if (!stack.isEmpty())
			return stack.peek();
		throw new ContextException("<getNumberStack> cannot be executed: stack is empty");
	}

	public void define(String name, double number) {
		definedParams.merge(name, number, (oldN, newN) -> newN);
	}

	public boolean hasParam(String param) {
		return definedParams.containsKey(param);
	}

	public double getNumberMap(String param) throws ContextException {
		if (!definedParams.isEmpty())
			return definedParams.get(param);
		throw new ContextException("<getNumberMap> cannot be executed: " +
					"list of defined parameters is empty");
	}

	public void clear() {
		definedParams.clear();
		stack.clear();
	}
}