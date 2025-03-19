package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.NotEnoughArguments;
import exceptions.ContextException;

class TestCommandPush {
	private Context c;
	private CommandPush pu;

	@BeforeEach
	void init() {
		c = new Context();
		pu = new CommandPush();
	}

	@Test
	void testEmptyArguments() {
		assertThrows(NotEnoughArguments.class, () -> pu.process(c, List.of()));
	}

	@Test
	void testArgumentsAndPushingNumber() throws Exception {
		pu.process(c, List.of("52"));

		assertEquals(52, c.pop());
	}

	@Test
	void testArgumentsAndPushingNoParameter() {
		assertThrows(ContextException.class, () -> pu.process(c, List.of("Koe-chto")));
	}

	@Test
	void testArgumentsAndPushingParameter() throws Exception {
		CommandDefine de = new CommandDefine();

		de.process(c, List.of("Koe-chto", "52"));
		pu.process(c, List.of("Koe-chto"));
	}
}