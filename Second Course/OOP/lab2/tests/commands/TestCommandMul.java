package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;

class TestCommandMul {
	private Context c;
	private CommandMul m;

	@BeforeEach
	void init() {
		c = new Context();
		m = new CommandMul();
	}

	@Test
	void testNotEnoughContext() {
		c.push(52);

		assertThrows(ContextException.class, () -> m.process(c, List.of()));
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> m.process(c, List.of()));
	}

	@Test
	void testArgumentsAndMultiplication() throws Exception {
		c.push(12.5f);
		c.push(8.4f);

		m.process(c, List.of("Koe-chto"));

		assertEquals(105.0f, c.pop(), Math.ulp(105.0f));
	}
}