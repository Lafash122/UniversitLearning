package game;

import javax.swing.*;
import java.awt.*;

public class GraphicController {
	public ComID getLaunchCommand(GraphicView view) {
		final ComID[] launchCommand = {ComID.DEFAULT};

		view.getAboutButton().addActionListener(e -> launchCommand[0] = ComID.ABOUT);
		view.getHighScoresButton().addActionListener(e -> launchCommand[0] = ComID.SCORES);
		view.getNewGameButton().addActionListener(e -> launchCommand[0] = ComID.GAME);
		view.getExitButton().addActionListener(e -> launchCommand[0] = ComID.EXIT);

		while(launchCommand[0].compareTo(ComID.EXIT) > 0) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return launchCommand[0];
	}

	public ComID getNextRoundCommand(int type) {
		if (type == 2)
			return ComID.SCORES;
		else if (type == 1)
			return ComID.EXIT;

		return ComID.NEXT;
	}

	public ComID getRoundCommand(int type) {
		if (type == JOptionPane.YES_OPTION)
			return ComID.YES;
		
		return ComID.NO;
	}
	
	public String getPlayerName(GraphicView view, int type) {
		if (type != JOptionPane.OK_OPTION)
			return "UnknownPlayer";

		String name = view.getPlayerNameField().getText();
		if (name.isEmpty())
			return "UnknownPlayer";

		return name.replaceAll("[;,.]", "_");
	}

	public double getBet(GraphicView view, int type) {
		if (type != JOptionPane.OK_OPTION)
			return 0;

		String line = view.getBetField().getText();
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