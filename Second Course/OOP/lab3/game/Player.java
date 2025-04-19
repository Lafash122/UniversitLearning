package game;

import java.util.List;
import java.util.ArrayList;

public class Player {
	private String name;
	private int gameScore;
	private double cash;

	private int handScore;
	private List<Kard> hand;

	public Player(String playerName) {
		gameScore = 0;
		cash = 1000;
		name = playerName;
		handScore = 0;
		hand = new ArrayList<>();
	}

	public void makeBet(double bet) {
		if (bet >= cash) {
			System.out.println("Your bet is more than you have. All your money will be wagered");
			cash = 0;
			return;
		}

		cash -= bet;
	}

	public void takeBet(double ratio, double bet) {
		cash += bet * ratio;
	}

	public void updateHandScore() {
		int aceInfluence = 0;
		handScore = 0;
		for (Kard k : hand) {
			if (k.getValueId() == 1) {
				handScore += 11;
				aceInfluence += 1;
			}
			else if (k.getValueId() > 10)
				handScore += 10;
			else
				handScore += k.getValueId();
		}

		while ((handScore > 21) && (aceInfluence > 0)) {
			handScore -= 10;
			aceInfluence -= 1;
		}
	}

	public void updateGameScore() {
		gameScore = ((int) cash - 1000);
	}

	public void takeKard(Kard k) {
		hand.add(k);
	}

	public List<Kard> getHand() {
		return hand;
	}

	public List<Kard> giveHand() {
		List<Kard> outHand = hand;
		hand = new ArrayList<>();
		return outHand;
	}

	public int getHandScore() {
		return handScore;
	}

	public int getGameScore() {
		return gameScore;
	}

	public String getName() {
		return name;
	}

	public double getCash() {
		return cash;
	}
}