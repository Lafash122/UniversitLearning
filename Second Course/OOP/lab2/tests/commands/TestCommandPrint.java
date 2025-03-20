package commands;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import tools.Context;
import exceptions.ContextException;
import exceptions.CalculatorException;

class TestCommandPrint {
	private Context c;
	private CommandPrint pr;

	@BeforeEach
	void init() {
		c = new Context();
		pr = new CommandPrint();
	}

	@Test
	void testEmptyContext() {
		assertThrows(ContextException.class, () -> pr.process(c, List.of()));
	}

	@Test
	void testArgumentsAndExecution() throws CalculatorException {
		c.push(52);

		assertDoesNotThrow(() -> pr.process(c, List.of()));
	}
}