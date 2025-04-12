package game;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Model {
	private int deckNum;
	private List<Kard> deck;
	private Dealer d;
	private Player p;

	private ConsoleView v;

	public Model(int num) {
		deckNum = num;
	}

	public void genDeck() {
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


	public void round() {
		Scanner sc = new Scanner(System.in);

		shuffleDeck();

		p.makeBet(100);
		p.takeKard(deck.remove(deck.size() - 1));
		p.takeKard(deck.remove(deck.size() - 1));
		p.showHand();
		p.updateHandScore();
		System.out.println(p.getHandScore());

		d.takeKard(deck.remove(deck.size() - 1));
		d.takeKard(deck.remove(deck.size() - 1));
		d.showKard();
		System.out.println(d.showPublicScore());

		if ((p.getHandScore() == 21) && (d.showPublicScore() < 10)) {
			List<Kard> pHand = p.giveHand();
			List<Kard> dHand = d.giveHand();
			deck.addAll(pHand);
			deck.addAll(dHand);

			p.takeBet(2.5f);
			p.updateGameScore();
			System.out.println(p.getGameScore());
			return;
		}

		else if ((p.getHandScore() == 21) && (d.showPublicScore() >= 10)) {
			int flag = sc.nextInt();
			if (flag > 0) {
				List<Kard> pHand = p.giveHand();
				List<Kard> dHand = d.giveHand();
				deck.addAll(pHand);
				deck.addAll(dHand);

				p.takeBet(2.0f);
				p.updateGameScore();
				System.out.println(p.getGameScore());
				return;
			}

			d.updateHandScore();
			while(d.getHandScore() < 17) {
				d.takeKard(deck.remove(deck.size() - 1));
				d.updateHandScore();
			}

			d.showHand();
			System.out.println(d.getHandScore());

			if (d.getHandScore() != 21) {
				List<Kard> pHand = p.giveHand();
				List<Kard> dHand = d.giveHand();
				deck.addAll(pHand);
				deck.addAll(dHand);

				p.takeBet(2.5f);
				p.updateGameScore();
				System.out.println(p.getGameScore());
				return;
			}

			List<Kard> pHand = p.giveHand();
			List<Kard> dHand = d.giveHand();
			deck.addAll(pHand);
			deck.addAll(dHand);

			p.takeBet(1.0f);
			p.updateGameScore();
			System.out.println(p.getGameScore());
			return;
		}

		int flag = sc.nextInt();
		while((flag > 0) && (p.getHandScore() < 21)) {
			p.takeKard(deck.remove(deck.size() - 1));
			p.showHand();
			p.updateHandScore();
			System.out.println(p.getHandScore());
			flag = sc.nextInt();
		}

		if (p.getHandScore() > 21) {
			List<Kard> pHand = p.giveHand();
			List<Kard> dHand = d.giveHand();
			deck.addAll(pHand);
			deck.addAll(dHand);

			p.takeBet(0.0f);
			p.updateGameScore();
			System.out.println(p.getGameScore());
			return;
		}

		d.updateHandScore();
		while(d.getHandScore() < 17) {
			d.takeKard(deck.remove(deck.size() - 1));
			d.updateHandScore();
		}
		d.showHand();
		System.out.println(d.getHandScore());

		List<Kard> pHand = p.giveHand();
		List<Kard> dHand = d.giveHand();
		deck.addAll(pHand);
		deck.addAll(dHand);

		if (((d.getHandScore() == 21) && (d.getHandNum() == 2)) || ((d.getHandScore() > p.getHandScore()) && (d.getHandScore() <= 21))) {
			p.takeBet(0.0f);
			p.updateGameScore();
			System.out.println(p.getGameScore());
			return;
		}
		else if (d.getHandScore() == p.getHandScore()) {
			p.takeBet(1.0f);
			p.updateGameScore();
			System.out.println(p.getGameScore());
			return;
		}
		p.takeBet(2.0f);
		p.updateGameScore();
		System.out.println(p.getGameScore());
	}

	public void game(String playerName) {
		genDeck();

		p = new Player(playerName);
		d = new Dealer();

		round();
	}

	public void printDeck() {
		for (Kard k : deck)
			System.out.println(k);
	}
}