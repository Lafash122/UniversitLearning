package game;

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

	public void getPlayerNameQuery() {
		System.out.print("Enter your game name: ");
	}

	//public void showPlayerStatus(Player player) {
	//	System.out.println("Player hand:");
	//	for (Kard k : )
	//}

	//public void showTable(Dealer dealer, Player player) {
	//	System.out.println("=====================");
	//}
}