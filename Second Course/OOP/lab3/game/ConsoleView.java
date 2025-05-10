package game;

import java.util.List;
import java.util.Map;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ConsoleView {
	private void showInfoFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			System.out.println("The file \'" + filePath + "\' not found");
			System.exit(1);
		}

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null)
				System.out.println(line);
			System.out.println();
		}
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

	public void greeting() {
		showInfoFile("resourses/greeting.txt");
	}

	public void about() {
		showInfoFile("resourses/about.txt");
	}

	public int getPlayerNameQuery() {
		System.out.print("Enter your game name: ");
		return 0;
	}

	public int getPlayerBetQuery(double cash) {
		System.out.print("Enter the amount you want to bet (Now you have " + cash + "): ");
		return 0;
	}

	public int getInsuranceQuery() {
		System.out.print("\nDo you want to make Insurance? [yes/no]: ");
		return 0;
	}

	public int getTakeKardQuery() {
		System.out.print("\nDo you want to make one more Card? [yes/no]: ");
		return 0;
	}

	public int getNextRoundQuery() {
		System.out.print("\nDo you want to continue game? Write \'next\' if yes, \'exit\' if no: ");
		return 0;
	}

	public void messageWrongCom() {
		System.out.println("Wrong Command");
	}

	public void messageSaveScore(boolean flag) {
		if (flag)
			System.out.println("Your score was saved");
	}

	public void showScores(Map<String, Integer> scores, boolean flag) {
		if (flag) {
			System.out.println("High Scores Table:");
			for (Map.Entry<String, Integer> entry : scores.entrySet())
  				System.out.println(entry.getKey() + " " + entry.getValue());
		}

		System.out.print("\n");
	}

	public void showPlayerRoundInfo(List<Kard> hand, int score) {
		System.out.println("\nYour hand:\nScore: " + score + "\nCards:");
		for (Kard k : hand)
			System.out.println(k);
	}

	public void showDealerRoundInfo(Kard publicKard, int handSize, int score) {
		System.out.println("\nDealer hand:\nDisplayed score: " + score +
				"\nTotal cards: " + handSize + "\nDisplayed card: ");
		System.out.println(publicKard);
	}

	public void showDealerHand(List<Kard> hand, int score) {
		System.out.println("\nDealer hand:\nScore: " + score + "\nCards:");
		for (Kard k : hand)
			System.out.println(k);
	}

	public void winBJ(double ratio, double bet, int score) {
		System.out.print("\nBlackJack! ");
		System.out.println("You win: " + (ratio * bet) + "\nYour game score: " + score);
	}

	public void win(double ratio, double bet, int score) {
		System.out.println("\nYou win: " + (ratio * bet) + "\nYour game score: " + score);
	}

	public void draw(int score) {
		System.out.println("\nDraw! Your game score: " + score);
	}

	public void lose(double bet, int score) {
		System.out.println("\nYou lose: " + bet + "\nYour game score: " + score);
	}
}