package game;

import java.util.List;
import java.util.ArrayList;

public class Dealer {
	private int handScore;
	private int kardNum;
	private int publicHandScore;
	private List<Kard> hand;

	public Dealer() {
		kardNum = 0;
		publicHandScore = 0;
		hand = new ArrayList<>();
	}

	public void takeKard(Kard k) {
		hand.add(k);
		if (hand.size() == 1)
			if (k.getValueId() == 1)
				publicHandScore = 11;
			else if (k.getValueId() > 10)
				publicHandScore = 10;
			else
				publicHandScore = k.getValueId();
	}

	public List<Kard> giveHand() {
		List<Kard> outHand = hand;
		hand = new ArrayList<>();
		return outHand;
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

	public void showKard() {
		System.out.println("\nDealer hand: ");
		if (!hand.isEmpty()) {
			System.out.println(hand.get(0));
			return;
		}
		System.out.println("Unknown Kard");
	}

	public void showHand() {
		System.out.println("\nDealer hand:");
		for (Kard k : hand)
			System.out.println(k);
	}

	public int showPublicScore() {
		return publicHandScore;
	}

	public int getHandScore() {
		return handScore;
	}

	public int getHandNum() {
		return kardNum;
	}
}