package game;

import java.util.Scanner;

public class ConsoleController {
	private Scanner reader;

	public ConsoleController() {
		reader = new Scanner(System.in);
	}

	public int getLaunchCommand(ConsoleView viewer) {
		int res = 0;
		String command = reader.nextLine();
		switch (command.toLowerCase()) {
			case "a":
			case "about":
				res = 1;
				break;
			case "h":
			case "high scores":
				res = 2;
				break;
			case "n":
			case "new game":
				res = 3;
				break;
			case "e":
			case "exit":
				res = 4;
				break;
			default:
				res = -1;
				break;
		}

		return res;
	}

	public int getNextRoundCommand(int type) {
		int res = 0;
		String command = reader.nextLine();
		switch (command.toLowerCase()) {
			case "y":
			case "yes":
			case "nx":
			case "next":
				res = 1;
				break;
			case "s":
			case "save":
				res = 2;
				break;
			case "n":
			case "no":
			case "e":
			case "exit":
				res = 3;
				break;
			default:
				res = -1;
				break;
		}

		return res;
	}

	public int getRoundCommand(int type) {
		int res = 0;
		String command = reader.nextLine();
		switch (command.toLowerCase()) {
			case "yes":
			case "y":
				res = 1;
				break;
			case "no":
			case "n":
				res = 2;
				break;
			case "double":
				res = 3;
				break;
			default:
				res = -1;
				break;
		}

		return res;
	}

	public String getPlayerName(ConsoleView viewer, int type) {
		String line = reader.nextLine();
		if (line.isEmpty())
			return "UnknownPlayer";

		return line.replaceAll("[;,.]", "_");
	}

	public double getBet(ConsoleView viewer, int type) {
		String line = reader.nextLine();
		double bet;
		try {
			bet = Double.parseDouble(line);
		}
		catch (Exception e) {
			bet = 0;
		}

		return bet;
	}
}