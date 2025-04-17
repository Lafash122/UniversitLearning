package game;

public class Game {
	private Model model;
	private ConsoleView viewer;
	private ConsoleController controller;	

	public Game() {
		model = new Model(1);
		viewer = new ConsoleView();
		controller = new ConsoleController();
	}

	public void launch() {
		viewer.greeting();
		int launchComID = 0;
		while (launchComID < 3) {
			launchComID = controller.getLaunchCommand();
			if (launchComID == 1)
				viewer.about();
			else if (launchComID == 2)
				System.out.println("High Scores Table");
			else if (launchComID == 3)
				newGame();
			else if (launchComID == 4)
				System.exit(0);
			else
				System.out.println("Wrong command");
		}	
	}

	public void newGame() {
		viewer.getPlayerNameQuery();
		model.initGame(controller.getPlayerName());
		model.round();
		int gameComID = 0;
		while (gameComID < 3) {
			gameComID = controller.getNextRoundCommand();
			if (gameComID == 1)
				model.round();
			else if (gameComID == 2)
				System.out.println("Saving Score Table");
			else if (gameComID == 3)
				System.exit(0);
			else
				System.out.println("Wrong command");
		}
	}

	public void newRound() {
	}
}