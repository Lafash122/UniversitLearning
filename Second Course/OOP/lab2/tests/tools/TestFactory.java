package tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import commands.Command;
import exceptions.UnknownCommand;
import exceptions.ConfigFileNotFound;

class TestFactory {
	private CommandFactory f;

	@BeforeEach
	void init() throws ConfigFileNotFound {
		f = new CommandFactory();
	}

	@Test
	void testUnknownCommand() {
		assertThrows(UnknownCommand.class, () -> f.getCommand("Comanda"));
	}

	@Test
	void testCheckAllCommands() throws ConfigFileNotFound {
		assertDoesNotThrow(() -> f.getCommand("+"));
		assertDoesNotThrow(() -> f.getCommand("#"));
		assertDoesNotThrow(() -> f.getCommand("DEFINE"));
		assertDoesNotThrow(() -> f.getCommand("/"));
		assertDoesNotThrow(() -> f.getCommand("EXIT"));
		assertDoesNotThrow(() -> f.getCommand("*"));
		assertDoesNotThrow(() -> f.getCommand("POP"));
		assertDoesNotThrow(() -> f.getCommand("PRINT"));
		assertDoesNotThrow(() -> f.getCommand("PUSH"));
		assertDoesNotThrow(() -> f.getCommand("SQRT"));
		assertDoesNotThrow(() -> f.getCommand("-"));
	}
}