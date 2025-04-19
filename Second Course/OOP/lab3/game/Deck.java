package game;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class Deck {
	private List<Kard> deck;

	public Deck(int deckNum) {
		genDeck(deckNum);
	}

	private void genDeck(int deckNum) {
		deck = new ArrayList<>();

		for (int k = 0; k < deckNum; k++) {
			for (int i = 0; i < 4; i++) {
				for (int j = 1; j <= 13; j++) {
					String suit = "Suit";
					switch (i) {
						case 0:
							suit = "Diamonds";
							break;
						case 1:
							suit = "Clubs";
							break;
						case 2:
							suit = "Hearts";
							break;
						case 3:
							suit = "Spades";
							break;
					}

					deck.add(new Kard(suit, j));
				}
			}
		}
	}

	public void shuffleDeck() {
		Collections.shuffle(deck);
	}

	public Kard removeKard() {
		return deck.remove(deck.size() - 1);
	}

	public void mergeKardDeck(List<Kard> hand) {
		deck.addAll(hand);
	}

	public void printDeck() {
		for (Kard k : deck)
			System.out.println(k);
	}
}