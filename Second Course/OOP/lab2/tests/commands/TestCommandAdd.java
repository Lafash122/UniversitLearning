package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;

class TestCommandAdd {
	private Context c;
	private CommandAdd a;

	@BeforeEach
	void init() {
		c = new Context();
		a = new CommandAdd();
	}

	@Test
	void testArgumentsAndAddition() throws ContextException {
		c.push(12.5f);
		c.push(8.4f);

		a.process(c, List.of("Koe-chto"));

		assertEquals(20.9f, c.pop(), Math.ulp(20.9f));
	}

	@Test
	void testNotEnoughContext() {
		c.push(5);

		assertThrows(ContextException.class, () -> a.process(c, List.of()));
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> a.process(c, List.of()));
	}
}