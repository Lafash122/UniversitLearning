package app.gameStructures;

public interface GameStateListener {
    void onGameStateChanged(GameState newState);
    void onGameStarted();
    void onGameStopped();
}