package game;

public class Game {
	private Model model;
	private GraphicView viewer;
	private GraphicController controller;
	private ScoreTableHandler scores;
	private boolean isScoresLoad = false;

	public Game() {
		model = new Model();
		viewer = new GraphicView();
		controller = new GraphicController();
		scores = new ScoreTableHandler();
	}

	public void launch() {
		viewer.greeting();
		ComID launchComID = ComID.DEFAULT;
		while (launchComID != ComID.GAME) {
			launchComID = controller.getLaunchCommand(viewer);
			if (launchComID == ComID.ABOUT)
				viewer.about();
			else if (launchComID == ComID.SCORES) {
				boolean success = scores.readScores();
				viewer.showScores(scores.getScores(), success);
			}
			else if (launchComID == ComID.GAME)
				newGame();
			else if (launchComID == ComID.EXIT)
				System.exit(0);
			else
				viewer.messageWrongCom();
		}	
	}

	public void newGame() {
		int decision_name = viewer.getPlayerNameQuery();
		model.initGame(controller.getPlayerName(viewer, decision_name));
		newRound();
		ComID gameComID = ComID.DEFAULT;
		while (gameComID != ComID.EXIT) {
			int decision_round = viewer.getNextRoundQuery();
			gameComID = controller.getNextRoundCommand(decision_round);
			if (gameComID == ComID.NEXT)
				newRound();
			else if (gameComID == ComID.SCORES) {
				if (!isScoresLoad)
					isScoresLoad = scores.readScores();
				boolean success = scores.writeScores(model.getPlayerName(), model.getPlayerGameScore());
				viewer.messageSaveScore(success);
			}
			else if (gameComID == ComID.EXIT)
				System.exit(0);
			else
				viewer.messageWrongCom();
		}
	}

	public void newRound() {
		int decision_bet = viewer.getPlayerBetQuery(model.getPlayerCash());
		double bet = controller.getBet(viewer, decision_bet);
		model.initRound(bet);
		viewer.showPlayerRoundInfo(model.getPlayerHand(), model.getPlayerHandScore());
		viewer.showDealerRoundInfo(model.getDealerPublicKard(), model.getDealerHandSize(), model.getDealerPublicScore());

		if ((model.getPlayerHandScore() == 21) && (model.getDealerPublicScore() < 10)) {
			model.endRound(2.5f);
			viewer.winBJ(1.5f, bet, model.getPlayerGameScore());
			return;
		}
		else if ((model.getPlayerHandScore() == 21) && (model.getDealerPublicScore() >= 10)) {
			int decision_insurance = viewer.getInsuranceQuery();
			ComID roundComID = controller.getRoundCommand(decision_insurance);
			viewer.showDealerHand(model.getDealerHand(), model.getDealerHandScore());

			if (roundComID == ComID.YES) {
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

		int decision_card = viewer.getTakeKardQuery();
		while ((model.getPlayerHandScore() < 21) && (controller.getRoundCommand(decision_card) != ComID.NO)) {
			model.playerTakeKard();
			viewer.showPlayerRoundInfo(model.getPlayerHand(), model.getPlayerHandScore());
			if (model.getPlayerHandScore() < 21)
				decision_card = viewer.getTakeKardQuery();
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