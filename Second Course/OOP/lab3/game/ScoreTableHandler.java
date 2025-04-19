package game;

import java.io.File;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Map;

public class ScoreTableHandler {
	private final File file;
	private Map<String, Integer> scores;

	public ScoreTableHandler() {
		file = new File("scores/score_table.csv");
		scores = new TreeMap<>();
	}

	public boolean readScores() {
		if (!file.exists()) {
			System.out.println("The file \'scores/score_table.csv\' not found");
			return false;
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] playerData = line.split(";");
				String name = playerData[0];
				int score = Integer.parseInt(playerData[1]);
				scores.merge(name, score, (oldN, newN) -> newN);
			}
		}
		catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			return false;
		}

		return true;
	}

	public boolean writeScores(String name, int score) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			}
			catch (IOException e) {
				System.out.println("Error: " + e.getMessage());
				return false;
			}
		}

		scores.merge(name, score, (oldN, newN) -> newN);

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
			scores.entrySet().stream().sorted((a, b) -> {
				int cmp = b.getValue().compareTo(a.getValue());
				return (cmp != 0) ? cmp : a.getKey().compareTo(b.getKey());
			}).forEach(entry -> {
				try {
					writer.write(entry.getKey() + ";" + entry.getValue());
					writer.newLine();
				}
				catch(IOException e) {
					System.out.println("Error: " + e.getMessage());
					throw new RuntimeException(e);
				}
			});
		}
		catch (IOException | RuntimeException e) {
			System.out.println("Error: " + e.getMessage());
			return false;
		}

		return true;
	}

	public Map<String, Integer> getScores() {
		return scores;
	}
}