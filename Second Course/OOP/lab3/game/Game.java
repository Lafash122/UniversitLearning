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
			switch (launchComID) {
				case 1:
					viewer.about();
					break;
				case 2:
					System.out.println("High Scores Table");
					break;
				case 3:
					newGame("John");
					break;
				case 4:
					System.exit(0);
					break;
				default:
					System.out.println("Wrong command");
					break;
			}
		}	
	}

	public void newGame(String name) {
		model.initGame(name);
		model.game();
	}

	public void round() {
	}
}