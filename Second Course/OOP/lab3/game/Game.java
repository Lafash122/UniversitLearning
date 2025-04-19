package game;

public class Game {
	private Model model;
	private ConsoleView viewer;
	private ConsoleController controller;
	private ScoreTableHandler scores;

	public Game() {
		model = new Model();
		viewer = new ConsoleView();
		controller = new ConsoleController();
		scores = new ScoreTableHandler();
	}

	public void launch() {
		viewer.greeting();
		int launchComID = 0;
		while (launchComID < 3) {
			launchComID = controller.getLaunchCommand();
			if (launchComID == 1)
				viewer.about();
			else if (launchComID == 2) {
				boolean success = scores.readScores();
				viewer.showScores(scores.getScores(), success);
			}
			else if (launchComID == 3)
				newGame();
			else if (launchComID == 4)
				System.exit(0);
			else
				viewer.messageWrongCom();
		}	
	}

	public void newGame() {
		viewer.getPlayerNameQuery();
		model.initGame(controller.getPlayerName());
		newRound();
		int gameComID = 0;
		while (gameComID < 3) {
			viewer.getNextRoundQuery();
			gameComID = controller.getNextRoundCommand();
			if (gameComID == 1)
				newRound();
			else if (gameComID == 2) {
				boolean success = scores.writeScores(model.getPlayerName(), model.getPlayerGameScore());
				viewer.messageSaveScore(success);
			}
			else if (gameComID == 3)
				System.exit(0);
			else
				viewer.messageWrongCom();
		}
	}

	public void newRound() {
		viewer.getPlayerBetQuery(model.getPlayerCash());
		double bet = controller.getBet();
		model.initRound(bet);
		viewer.showPlayerRoundInfo(model.getPlayerHand(), model.getPlayerHandScore());
		viewer.showDealerRoundInfo(model.getDealerPublicKard(), model.getDealerHandSize(), model.getDealerPublicScore());

		if ((model.getPlayerHandScore() == 21) && (model.getDealerPublicScore() < 10)) {
			model.endRound(2.5f);
			viewer.winBJ(1.5f, bet, model.getPlayerGameScore());
			return;
		}
		else if ((model.getPlayerHandScore() == 21) && (model.getDealerPublicScore() >= 10)) {
			viewer.getInsuranceQuery();
			int roundComID = controller.getRoundCommand();
			viewer.showDealerHand(model.getDealerHand(), model.getDealerHandScore());

			if (roundComID == 1) {
				model.endRound(2.0f);
				viewer.win(1.0f, bet, model.getPlayerGameScore());
				return;
			}

			if (model.getDealerHandScore() != 21) {
				model.endRound(2.5f);
				viewer.winBJ(1.5f, bet, model.getPlayerGameScore());
				return;
			}

			model.endRound(1.0f);
			viewer.draw(model.getPlayerGameScore());
			return;
		}

		viewer.getTakeKardQuery();
		while ((model.getPlayerHandScore() < 21) && (controller.getRoundCommand() != 2)) {
			model.playerTakeKard();
			viewer.showPlayerRoundInfo(model.getPlayerHand(), model.getPlayerHandScore());
			if (model.getPlayerHandScore() < 21)
				viewer.getTakeKardQuery();
		}

		if (model.getPlayerHandScore() > 21) {
			model.endRound(0.0f);
			viewer.lose(bet, model.getPlayerGameScore());
			return;
		}

		while (model.getDealerHandScore() < 17) {
			model.dealerTakeKard();
		}
		viewer.showDealerHand(model.getDealerHand(), model.getDealerHandScore());

		if (((model.getDealerHandScore() == 21) && (model.getDealerHandSize() == 2)) || ((model.getDealerHandScore() > model.getPlayerHandScore()) && (model.getDealerHandScore() <= 21))) {
			model.endRound(0.0f);
			viewer.lose(bet, model.getPlayerGameScore());
			return;
		}
		else if (model.getDealerHandScore() == model.getPlayerHandScore()) {
			model.endRound(1.0f);
			viewer.draw(model.getPlayerGameScore());
			return;
		}

		model.endRound(2.0f);
		viewer.win(1.0f, bet, model.getPlayerGameScore());
		return;
	}
}