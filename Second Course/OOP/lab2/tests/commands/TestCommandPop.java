package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;

class TestCommandPop {
	private Context c;
	private CommandPop po;

	@BeforeEach
	void init() {
		c = new Context();
		po = new CommandPop();
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> po.process(c, List.of()));
	}

	@Test
	void testArgumentsAndPop() throws Exception {
		c.push(12.5f);

		po.process(c, List.of("Koe-chto"));
		
		assertTrue(c.sizeStack() == 0);

		c.push(12.5f);
		c.push(8.4f);

		po.process(c, List.of("Koe-chto"));

		assertEquals(12.5f, c.pop(), Math.ulp(105.0f));
	}
}