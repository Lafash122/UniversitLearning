package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;
import exceptions.CalculatorException;

class TestCommandSub {
	private Context c;
	private CommandSub s;

	@BeforeEach
	void init() {
		c = new Context();
		s = new CommandSub();
	}

	@Test
	void testNotEnoughContext() {
		c.push(52);

		assertThrows(ContextException.class, () -> s.process(c, List.of()));
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> s.process(c, List.of()));
	}

	@Test
	void testArgumentsAndAddition() throws CalculatorException {
		c.push(8.4f);
		c.push(12.5f);

		s.process(c, List.of("Koe-chto"));

		assertEquals(4.1f, c.pop(), Math.ulp(4.1f));
	}
}