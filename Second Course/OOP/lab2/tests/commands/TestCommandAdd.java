package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;
import exceptions.CalculatorException;

class TestCommandAdd {
	private Context c;
	private CommandAdd a;

	@BeforeEach
	void init() {
		c = new Context();
		a = new CommandAdd();
	}

	@Test
	void testNotEnoughContext() {
		c.push(52);

		assertThrows(ContextException.class, () -> a.process(c, List.of()));
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> a.process(c, List.of()));
	}

	@Test
	void testArgumentsAndAddition() throws CalculatorException {
		c.push(12.5f);
		c.push(8.4f);

		a.process(c, List.of("Koe-chto"));

		assertEquals(20.9f, c.pop(), Math.ulp(20.9f));
	}
}