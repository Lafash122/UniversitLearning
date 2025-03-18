package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.NotEnoughArguments;
import exceptions.InvalidParameter;

class TestCommandDefine {
	private Context c;
	private CommandDefine de;

	@BeforeEach
	void init() {
		c = new Context();
		de = new CommandDefine();
	}

	@Test
	void testNotEnoughArguments() {
		assertThrows(NotEnoughArguments.class, () -> de.process(c, List.of("Koe-chto")));
	}

	@Test
	void testEmptyArgumentsList() {
		assertThrows(NotEnoughArguments.class, () -> de.process(c, List.of()));
	}

	@Test
	void testInvalidParameterName() {
		assertThrows(InvalidParameter.class, () -> de.process(c, List.of("Koe-chto1", "5")));
	}

	@Test
	void testInvalidParameterNumber() {
		assertThrows(Exception.class, () -> de.process(c, List.of("Koe-chto", "Number")));
	}

	@Test
	void testArgumentsAndDefining() throws Exception {
		de.process(c, List.of("Koe-chto", "1"));
		de.process(c, List.of("Cho-to", "9", "Escho"));

		assertTrue(c.hasParam("Koe-chto"));
		assertTrue(c.hasParam("Cho-to"));

		assertEquals(1, c.getNumberMap("Koe-chto"));
		assertEquals(9, c.getNumberMap("Cho-to"));
	}
}