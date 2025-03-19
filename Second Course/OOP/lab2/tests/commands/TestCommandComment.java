package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;

class TestCommandComment {
	@Test
	void testArgumentsAndExecution() {
		Context c = new Context();
		CommandComment co = new CommandComment();

		assertDoesNotThrow(() -> co.process(c, List.of("Koe-chto")));

		c.push(52);

		assertDoesNotThrow(() -> co.process(c, List.of()));
	}
}