package game;

import java.util.List;

public class Model {
	private Deck deck;
	private Dealer d;
	private Player p;
	private double playerBet;

	public void initGame(String playerName) {
		deck = new Deck(1);
		p = new Player(playerName);
		d = new Dealer();
	}

	public void initRound(double bet) {
		deck.shuffleDeck();

		playerBet = bet;
		p.makeBet(bet);

		p.takeKard(deck.removeKard());
		p.takeKard(deck.removeKard());
		p.updateHandScore();

		d.takeKard(deck.removeKard());
		d.takeKard(deck.removeKard());
		d.updateHandScore();
	}

	public void endRound(double ratio) {
		deck.mergeKardDeck(p.giveHand());
		deck.mergeKardDeck(d.giveHand());
		p.takeBet(ratio, playerBet);
		p.updateGameScore();
		playerBet = 0;
	}

	public void playerTakeKard() {
		p.takeKard(deck.removeKard());
		p.updateHandScore();
	}

	public void dealerTakeKard() {
		d.takeKard(deck.removeKard());
		d.updateHandScore();
	}


	public int getPlayerHandScore() {
		return p.getHandScore();
	}

	public int getPlayerGameScore() {
		return p.getGameScore();
	}

	public List<Kard> getPlayerHand() {
		return p.getHand();
	}

	public double getPlayerCash() {
		return p.getCash();
	}

	public String getPlayerName() {
		return p.getName();
	}


	public int getDealerHandSize() {
		return d.getHandSize();
	}

	public int getDealerPublicScore() {
		return d.getPublicScore();
	}

	public Kard getDealerPublicKard() {
		return d.getPublicKard();
	}

	public int getDealerHandScore() {
		return d.getHandScore();
	}

	public List<Kard> getDealerHand() {
		return d.getHand();
	}
}