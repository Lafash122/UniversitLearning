package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;
import exceptions.NegativeRoot;

class TestCommandSqrt {
	private Context c;
	private CommandSqrt sq;

	@BeforeEach
	void init() {
		c = new Context();
		sq = new CommandSqrt();
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> sq.process(c, List.of()));
	}

	@Test
	void testNegativeRoot() {
		c.push(-52);

		assertThrows(NegativeRoot.class, () -> sq.process(c, List.of()));
	}

	@Test
	void testArgumentsAndSqrt() throws Exception {
		c.push(123454321);

		sq.process(c, List.of("Koe-chto"));

		assertEquals(11111.0f, c.pop(), Math.ulp(11111.0f));
	}
}