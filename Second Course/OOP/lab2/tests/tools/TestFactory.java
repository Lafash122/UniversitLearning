package tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import commands.Command;
import exceptions.UnknownCommand;

class TestFactory {
	@Test
	void testUnknownCommand() {
		assertThrows(UnknownCommand.class, () -> CommandFactory.getCommand("Comanda"));
	}

	@Test
	void testCheckAllCommands() {
		assertDoesNotThrow(() -> CommandFactory.getCommand("+"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("#"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("DEFINE"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("/"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("EXIT"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("*"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("POP"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("PRINT"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("PUSH"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("SQRT"));
		assertDoesNotThrow(() -> CommandFactory.getCommand("-"));
	}
}