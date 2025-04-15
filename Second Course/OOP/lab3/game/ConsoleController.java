package game;

import java.util.Scanner;

public class ConsoleController {
	private Scanner reader;

	public ConsoleController() {
		reader = new Scanner(System.in);
	}

	public int getLaunchCommand() {
		int res = 0;
		String command = reader.nextLine();
		switch (command.toLowerCase()) {
			case "about":
				res = 1;
				break;
			case "high scores":
				res = 2;
				break;
			case "new game":
				res = 3;
				break;
			case "exit":
				res = 4;
				break;
			default:
				res = -1;
				break;
		}

		return res;
	}
}