package tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import exceptions.ContextException;
import exceptions.CalculatorException;

class TestContext {
	private Context c;

	@BeforeEach
	void init() {
		c = new Context();
	}

	@Test
	void testPopEmptyStack() {
		assertThrows(ContextException.class, () -> c.pop());
	}

	@Test
	void testPushAndPop() throws CalculatorException {
		c.push(52);

		assertTrue(c.sizeStack() == 1);
		assertEquals(52, c.pop());
		assertTrue(c.sizeStack() == 0);
	}

	@Test
	void testSize() {
		assertEquals(0, c.sizeStack());

		c.push(52);

		assertEquals(1, c.sizeStack());
	}

	@Test
	void testGetNumberFromEmptyStack() {
		assertThrows(ContextException.class, () -> c.getNumberStack());
	}

	@Test
	void testGetNumberFromStack() throws CalculatorException {
		c.push(52);

		assertEquals(52, c.getNumberStack());
		assertTrue(c.sizeStack() == 1);
	}

	@Test
	void testGetNumberFromEmptyMap() {
		assertThrows(ContextException.class, () -> c.getNumberMap("Koe-chto"));
	}

	@Test
	void testHasParameter() {
		c.define("Koe-chto", 52);

		assertTrue(c.hasParam("Koe-chto"));
		assertFalse(c.hasParam("Chto-to"));
	}

	@Test
	void testDefineAndGetNumberFromMap() throws CalculatorException {
		c.define("Koe-chto", 52);

		assertTrue(c.hasParam("Koe-chto"));
		assertEquals(52, c.getNumberMap("Koe-chto"));
		assertTrue(c.hasParam("Koe-chto"));
	}

	@Test
	void testClear() {
		c.push(52);
		c.define("Koe-chto", 52);

		assertTrue(c.hasParam("Koe-chto"));
		assertTrue(c.sizeStack() == 1);

		c.clear();

		assertFalse(c.hasParam("Koe-chto"));
		assertTrue(c.sizeStack() == 0);
	}
}
