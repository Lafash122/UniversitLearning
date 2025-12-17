package app.main;

public class Main {
    public static void main(String[] args) {
        GameCoordinator gameCoordinator = new GameCoordinator();
        gameCoordinator.getUIManager().start();
    }
}