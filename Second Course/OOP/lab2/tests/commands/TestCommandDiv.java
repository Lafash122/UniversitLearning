package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.DivisionByZero;
import exceptions.ContextException;

class TestCommandDiv {
	private Context c;
	private CommandDiv d;

	@BeforeEach
	void init() {
		c = new Context();
		d = new CommandDiv();
	}

	@Test
	void testNotEnoughContext() {
		c.push(52);

		assertThrows(ContextException.class, () -> d.process(c, List.of()));
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> d.process(c, List.of()));
	}

	@Test
	void testDivisionByZero() {
		c.push(0);
		c.push(1);

		assertThrows(DivisionByZero.class, () -> d.process(c, List.of()));
	}

	@Test
	void testArgumentsAndDivision() throws Exception {
		c.push(1);
		c.push(52);

		d.process(c, List.of("Koe-chto"));

		assertEquals(52.0f, c.pop(), Math.ulp(52.0f));
	}
}