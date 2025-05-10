package game;

import javax.swing.*;
import java.awt.*;

public class GraphicController {
	public int getLaunchCommand(GraphicView view) {
		final int[] launchCommand = {0};

		view.getAboutButton().addActionListener(e -> launchCommand[0] = 1);
		view.getHighScoresButton().addActionListener(e -> launchCommand[0] = 2);
		view.getNewGameButton().addActionListener(e -> launchCommand[0] = 3);
		view.getExitButton().addActionListener(e -> launchCommand[0] = 4);

		while(launchCommand[0] < 1) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return launchCommand[0];
	}

	public int getNextRoundCommand(int type) {
		if (type == 2)
			return 2;
		else if (type == 1)
			return 3;

		return 1;
	}

	public int getRoundCommand(int type) {
		if (type == JOptionPane.YES_OPTION)
			return 1;
		
		return 2;
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