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

	public int getNextRoundCommand() {
		int res = 0;
		String command = reader.nextLine();
		switch (command.toLowerCase()) {
			case "next":
				res = 1;
				break;
			case "save":
				res = 2;
				break;
			case "exit":
				res = 3;
				break;
			default:
				res = -1;
				break;
		}

		return res;
	}

	public int getRoundCommand() {
		int res = 0;
		String command = reader.nextLine();
		switch (command.toLowerCase()) {
			case "yes":
				res = 1;
				break;
			case "y":
				res = 1;
				break;
			case "no":
				res = 2;
				break;
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

	public String getPlayerName() {
		String line = reader.nextLine();
		return line.replaceAll("[;,.]", "_");
	}

	public double getBet() {
		String line = reader.nextLine();
		double bet;
		try {
			bet = Double.parseDouble(line);
		}
		catch (Exception e) {
			System.out.println("Your bet cannot be interpreted. A bet of 0 will be placed.");
			bet = 0;
		}

		return bet;
	}
}