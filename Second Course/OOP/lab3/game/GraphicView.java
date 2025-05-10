package game;

import javax.swing.*;
import java.awt.*;

import java.util.Map;
import java.util.List;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class GraphicView extends JFrame {
	private CardLayout layout = new CardLayout();
	private JPanel scenePanel;

	private JButton exitButton;
	private JButton newGameButton;
	private JButton aboutButton;
	private JButton highScoresButton;
	private JButton nextRoundButton;
	private JButton saveScoreButton;

	private JTextField playerNameField;
	private JTextField betField;

	private JLabel balance;
	private JLabel scoresLabel;
	private JTextArea dealerInfo;
	private JTextArea playerInfo;
	private JPanel dealerHand;
	private JPanel playerHand;

	private static final Font standartFont = new Font("GOST Type BU", Font.BOLD, 15);
	private static final Font bigFont = new Font("GOST Type BU", Font.BOLD, 24);
	private static final Color gameGreen = new Color(40, 128, 85);
	private static final Color gameLGreen = new Color(0, 175, 149);
	private static final Color gameRed = new Color(210, 2, 2);

	private static int openedAboutWindows = 0;
	private static int openedScoresWindows = 0;
	private static double cash = 1000;

	public GraphicView() {
		setGlobalStyle();
		setTitle("BlackJack");
		setSize(700, 430);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);

		scenePanel = new JPanel(layout);

		scenePanel.add(createMenu(), "menu");
		scenePanel.add(createGame(), "game");

		add(scenePanel);
		setVisible(true);
	}

	private void setGlobalStyle() {
		UIManager.put("OptionPane.background", gameGreen);
		UIManager.put("Panel.background", gameGreen);
		UIManager.put("OptionPane.messageForeground", Color.WHITE);
		UIManager.put("OptionPane.messageFont", standartFont);
		UIManager.put("Button.background", gameLGreen);
		UIManager.put("Button.foreground", Color.WHITE);
		UIManager.put("Button.font", standartFont);
		UIManager.put("Table.background", gameGreen);
		UIManager.put("Table.foreground", Color.WHITE);
		UIManager.put("Table.selectionBackground", gameLGreen);
		UIManager.put("Table.font", standartFont);
		UIManager.put("Table.gridColor", gameRed);
		UIManager.put("TextArea.background", gameGreen);
		UIManager.put("TextArea.foreground", Color.WHITE);
		UIManager.put("TextArea.selectionBackground", gameLGreen);
		UIManager.put("TextArea.font", standartFont);
		UIManager.put("Label.foreground", Color.WHITE);
	}

	private JPanel createMenu() {
		JPanel menu = new JPanel();
		menu.setLayout(null);

		newGameButton = new JButton("New Game");
		aboutButton = new JButton("About");
		highScoresButton = new JButton("High Scores");
		exitButton = new JButton("Exit");

		newGameButton.setBounds(50, 120, 150, 50);
		aboutButton.setBounds(50, 180, 150, 50);
		highScoresButton.setBounds(50, 240, 150, 50);
		exitButton.setBounds(50, 300, 150, 50);

		menu.add(newGameButton);
		menu.add(aboutButton);
		menu.add(highScoresButton);
		menu.add(exitButton);

		return menu;
	}

	private JPanel makeHand(String title, JTextArea info, JPanel cards) {
		JPanel hand = new JPanel();
		hand.setLayout(new BorderLayout());

		JLabel handTitle = new JLabel(title, SwingConstants.CENTER);
		handTitle.setFont(bigFont);
		hand.add(handTitle, BorderLayout.NORTH);

		JPanel handData = new JPanel();
		handData.setLayout(new BoxLayout(handData, BoxLayout.X_AXIS));

		info.setEditable(false);
		info.setFont(standartFont);
		JScrollPane scroll = new JScrollPane(info);
		scroll.setBorder(null);
		scroll.setPreferredSize(new Dimension(150, 100));
		handData.add(scroll);

		cards.setPreferredSize(new Dimension(500, 100));
		handData.add(cards);

		hand.add(handData, BorderLayout.CENTER);

		return hand;
	}

	private JPanel createGame() {
		JPanel game = new JPanel();
		game.setLayout(new BoxLayout(game, BoxLayout.Y_AXIS));

		JPanel stats = new JPanel(new GridLayout(1, 2));
		stats.setPreferredSize(new Dimension(800, 60));

		balance = new JLabel("You have: $" + cash);
		balance.setFont(bigFont);
		balance.setHorizontalAlignment(SwingConstants.CENTER);
		stats.add(balance);
		scoresLabel = new JLabel("Your game score: 0");
		scoresLabel.setFont(bigFont);
		scoresLabel.setHorizontalAlignment(SwingConstants.CENTER);
		stats.add(scoresLabel);
		
		game.add(stats);

		JPanel hands = new JPanel(new GridLayout(2, 1));

		dealerInfo = new JTextArea("Hand score: 0\nTotal cards: 0");
		dealerHand = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hands.add(makeHand("Dealer Hand:", dealerInfo, dealerHand));

		playerInfo = new JTextArea("Hand score: 0\nTotal cards: 0");
		playerHand = new JPanel(new FlowLayout(FlowLayout.LEFT));
		hands.add(makeHand("Player Hand:", playerInfo, playerHand));

		game.add(hands);

		return game;
	}

	private void switchScene(String name) {
		layout.show(scenePanel, name);
	}

	public void greeting() {
		switchScene("menu");
	}

	public void about() {
		if (openedAboutWindows >= 1) {
			JOptionPane.showMessageDialog(null, "Exceeding the maximum possible number of open About windows", "BlackJack: Max About Windows", JOptionPane.PLAIN_MESSAGE);
			return;
		}

		JFrame about = new JFrame("BlackJack: About");
		about.setSize(640, 425);
		about.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		about.setResizable(false);

		JTextArea info = new JTextArea();
		info.setEditable(false);
		info.setBackground(gameGreen);
		info.setForeground(Color.WHITE);

		JScrollPane scroll = new JScrollPane(info);
		scroll.setBorder(null);
		about.add(scroll, BorderLayout.CENTER);

		File file = new File("resourses/about.txt");
		if (!file.exists())
			info.setText("The file \'resourses/about.txt\' not found");

		StringBuilder text = new StringBuilder();

		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null)
				text.append(line).append("\n");
			info.setText(text.toString());
		}
		catch (IOException e) {
			info.setText("Error: " + e.getMessage());
		}

		about.setVisible(true);

		openedAboutWindows++;

		about.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				openedAboutWindows--;
			}
		});
	}

	public int getPlayerNameQuery() {
		switchScene("game");
		playerNameField = new JTextField();
		Object[] message = { "Enter your game name:", playerNameField };
		return JOptionPane.showConfirmDialog(null, message, "BlackJack: Player Name", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public int getPlayerBetQuery(double cash) {
		betField = new JTextField();
		Object[] message = { "Enter the amount you want to bet (Now you have " + cash + "): ", betField };
		return JOptionPane.showConfirmDialog(null, message, "BlackJack: Player Bet", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public int getInsuranceQuery() {
		String message = "Do you want to make Insurance?";
		return JOptionPane.showConfirmDialog(null, message, "BlackJack: Insurance", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public int getTakeKardQuery() {
		String message = "Do you want to make one more Card?";
		return JOptionPane.showConfirmDialog(null, message, "BlackJack: Take Card", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public int getNextRoundQuery() {
		Object[] options = { "Next", "Exit", "Save"};
		return JOptionPane.showOptionDialog(null, "Do you want to continue game?", "BlackJack: Next Round", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
	}

	public void messageWrongCom() {
		JOptionPane.showMessageDialog(null, "Wrong Command", "BlackJack: Wrong Command", JOptionPane.PLAIN_MESSAGE);
	}

	public void messageSaveScore(boolean flag) {
		if (flag)
			JOptionPane.showMessageDialog(null, "Your score was saved", "BlackJack: Scores Save", JOptionPane.PLAIN_MESSAGE);
	}

	public void showScores(Map<String, Integer> scores, boolean flag) {
		if (openedScoresWindows >= 1) {
			JOptionPane.showMessageDialog(null, "Exceeding the maximum possible number of open Scores windows", "BlackJack: Max Scores Windows", JOptionPane.PLAIN_MESSAGE);
			return;
		}

		if (!flag) {
			JOptionPane.showMessageDialog(null, "Error: Scores Table Could Not Be Found", "BlackJack: Score Table Error", JOptionPane.PLAIN_MESSAGE);
			return;
		}

		String[][] data = new String[scores.size()][2];
		int i = 0;
		for (Map.Entry<String, Integer> entry : scores.entrySet()) {
			data[i][0] = entry.getKey();
			data[i][1] = String.valueOf(entry.getValue());
			i++;
		}

		String [] columnsName = {"", ""};

		JTable scoreTable = new JTable(data, columnsName) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		scoreTable.setTableHeader(null);
		JScrollPane scroll = new JScrollPane(scoreTable);
		scroll.getViewport().setBackground(gameLGreen);
		scroll.setBorder(null);

		JFrame score = new JFrame("BlackJack: About");
		score.setSize(640, 425);
		score.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		score.setResizable(false);
		score.add(scroll);
		score.setVisible(true);

		openedScoresWindows++;

		score.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				openedScoresWindows--;
			}
		});
	}

	public void showPlayerRoundInfo(List<Kard> hand, int score) {
		playerInfo.setText("Hand Score: " + score + "\nTotal cards: " + hand.size());
		playerHand.removeAll();
		for (Kard k : hand) {
			ImageIcon cardIco = new ImageIcon("resourses/" + k.getDesc() + ".png");
			Image card = cardIco.getImage();
			JLabel label = new JLabel(new ImageIcon(card.getScaledInstance(card.getWidth(null) * 2, card.getHeight(null) * 2, Image.SCALE_SMOOTH)));
			playerHand.add(label);
		}
		playerHand.revalidate();
		playerHand.repaint();
	}

	public void showDealerRoundInfo(Kard publicKard, int handSize, int score) {
		dealerInfo.setText("Hand score: " + score + " + ?\nTotal cards: " +
				handSize + "\nDisplayed score: " + score);
		dealerHand.removeAll();
		ImageIcon cardIco = new ImageIcon("resourses/" + publicKard.getDesc() + ".png");
		Image card = cardIco.getImage();
		JLabel label = new JLabel(new ImageIcon(card.getScaledInstance(card.getWidth(null) * 2, card.getHeight(null) * 2, Image.SCALE_SMOOTH)));
		dealerHand.add(label);

		ImageIcon cardBackIco = new ImageIcon("resourses/CardBack.png");
		Image cardBack = cardBackIco.getImage();
		JLabel labelBack = new JLabel(new ImageIcon(cardBack.getScaledInstance(cardBack.getWidth(null) * 2, cardBack.getHeight(null) * 2, Image.SCALE_SMOOTH)));
		for (int i = 0; i < handSize - 1; ++i) 
			dealerHand.add(labelBack);

		dealerHand.revalidate();
		dealerHand.repaint();
	}

	public void showDealerHand(List<Kard> hand, int score) {
		dealerInfo.setText("Hand score: " + score + "\nTotal cards: " +
				hand.size() + "\nDisplayed score: " + score);
		dealerHand.removeAll();
		for (Kard k : hand) {
			ImageIcon cardIco = new ImageIcon("resourses/" + k.getDesc() + ".png");
			Image card = cardIco.getImage();
			JLabel label = new JLabel(new ImageIcon(card.getScaledInstance(card.getWidth(null) * 2, card.getHeight(null) * 2, Image.SCALE_SMOOTH)));
			dealerHand.add(label);
		}
		dealerHand.revalidate();
		dealerHand.repaint();
	}

	public void winBJ(double ratio, double bet, int score) {
		JOptionPane.showMessageDialog(null, "BlackJack! You win: " + (ratio * bet) + "\nYour game score: " + score, "BlackJack: BlackJack", JOptionPane.PLAIN_MESSAGE);
		cash = cash + ratio * bet;
		balance.setText("You have: $" + cash);
		scoresLabel.setText("Your game score: " + score);
	}

	public void win(double ratio, double bet, int score) {
		JOptionPane.showMessageDialog(null, "You win: " + (ratio * bet) + "\nYour game score: " + score, "BlackJack: Win", JOptionPane.PLAIN_MESSAGE);
		cash = cash + ratio * bet;
		balance.setText("You have: $" + cash);
		scoresLabel.setText("Your game score: " + score);
	}

	public void draw(int score) {
		JOptionPane.showMessageDialog(null, "Draw! Your game score: " + score, "BlackJack: Draw", JOptionPane.PLAIN_MESSAGE);
		scoresLabel.setText("Your game score: " + score);
	}

	public void lose(double bet, int score) {
		JOptionPane.showMessageDialog(null, "You lose: " + bet + "\nYour game score: " + score, "BlackJack: Lose", JOptionPane.PLAIN_MESSAGE);
		cash = cash - bet;
		balance.setText("You have: $" + cash);
		scoresLabel.setText("Your game score: " + score);
	}


	public JButton getExitButton() {
		return exitButton;
	}

	public JButton getNewGameButton() {
		return newGameButton;
	}

	public JButton getAboutButton() {
		return aboutButton;
	}

	public JButton getHighScoresButton() {
		return highScoresButton;
	}

	public JTextField getPlayerNameField() {
		return playerNameField;
	}

	public JTextField getBetField() {
		return betField;
	}
}