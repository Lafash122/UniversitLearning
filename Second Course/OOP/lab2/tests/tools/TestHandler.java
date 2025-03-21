package tools;

import java.io.*;
import java.util.Scanner;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import commands.*;
import exceptions.*;

class TestHandler {
	@Test
	void testNoArguments() {
		String[] args = {};
		CalcHandler h = new CalcHandler(args);

		assertTrue(h.getMode());
	}

	@Test
	void testFileDoesNotExist() {
		String[] args = { "Kakoy_to_file.txt" };
		CalcHandler h = new CalcHandler(args);

		assertTrue(h.getMode());
	}

	@Test
	void testFileAndArguments() {
		String[] args = { "TestFile.txt", "Koe_chto" };
		File file = new File("TestFile.txt");

		try {
			file.createNewFile();
			CalcHandler h = new CalcHandler(args);

			assertFalse(h.getMode());

			file.delete();
		}
		catch (Exception e) {
			fail(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	@Test
	void testFileHandle() {
		File file = new File("TestFile.txt");

		try {
			file.createNewFile();
			file.deleteOnExit();

			try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
				w.write("PUSH 52");
				w.write("	   ");
				w.write("Command arg1 arg2 arg3");
			}
			catch (IOException i) {
				fail(i.getClass().getName() + " : " + i.getMessage());
			}

			String[] s = { "TestFile.txt" };

			CalcHandler h = new CalcHandler(s);
			h.fileHandle();
		}
		catch (Exception e) {
			fail(e.getClass().getName() + " : " + e.getMessage());
		}
	}

	@Test
	void testConsoleHandle() {
		String in = "PUSH 52\nComm arhf1 afe2e\n";

		InputStream stream = new ByteArrayInputStream(in.getBytes());
		System.setIn(stream);

		CalcHandler h = new CalcHandler(new String[] {});
		h.cmdHandle();
	}
}