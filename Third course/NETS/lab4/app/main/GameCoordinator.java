package app.main;

import app.enums.*;
import app.gameStructures.ConfigData;
import app.gameStructures.GamePlayer;
import app.gameStructures.GameState;
import app.network.NetworkManager;

import java.net.*;

import java.util.List;

public class GameCoordinator {
    private final UIManager uiManager;
    private GameEngine gameEngine;
    private NetworkManager networkManager;

    public GameCoordinator() {
        this.gameEngine = new GameEngine(this);
        this.networkManager = new NetworkManager(this);
        this.uiManager = new UIManager(this);
    }

    public UIManager getUIManager() {
        return uiManager;
    }

    public GameEngine getGameEngine() {
        return gameEngine;
    }

    public NetworkManager getNetworkManager() {
        return networkManager;
    }

    public void createNewGame(String gameName, String playerName) {
        networkManager.createGame(gameName);
        gameEngine.startGame(playerName, true);
        uiManager.showGame();
    }

    public void joinGame(String gameName, String playerName) {
        networkManager.joinRoom(gameName, playerName);
        gameEngine.startGame(playerName, false);
        uiManager.showGame();
    }

    public void sendSteer(Direction direction) {
        networkManager.sendSteerMessage(direction);
    }

    public List<String> getAvailableGames() {
        return networkManager.discoverGames();
    }

    public void exitToMenu() {
        networkManager.disconnect();
        gameEngine.stopGame();
        this.gameEngine = new GameEngine(this);
        this.networkManager = new NetworkManager(this);
        uiManager.showMainMenu();
        gameEngine.addGameStateListener(uiManager);
    }

    public void abortGame() {
        networkManager.disconnect();
        gameEngine.stopGame();
    }

    public void steerFromInput(Direction direction) {
        gameEngine.setDirection(networkManager.getPlayerID(), direction);
    }

    public void onGameStateReceived(GameState state) {
        gameEngine.updateFromNetwork(state);
        gameEngine.notifyGameStateChanged();
    }

    public void onSteerCommandReceived(int playerId, Direction direction, long msgSeq) {
        if (gameEngine.isMaster()) {
            gameEngine.addPendingSteer(playerId, direction, msgSeq);
        }
    }

    public void onPlayerJoined(String playerName, InetSocketAddress address, int ID, NodeRole role) {
        if (gameEngine.isMaster()) {
            boolean success = gameEngine.addNewPlayer(playerName, ID, role, address.getPort(), address.getAddress().getHostAddress());
            if (success) {
                networkManager.addPlayer(gameEngine.getLastPlayerId(), address);
            } else {
                networkManager.sendErrorMessage(address, "Cannot join room");
            }
        }
    }

    public void onPlayerDisconnected(int playerId) {
        gameEngine.removePlayer(playerId);
    }

    public void onGameStateUpdated(GameState state) {
        if (gameEngine.isMaster()) {
            networkManager.broadcastGameState(state);
        }
    }

    public void onRoleChanged(NodeRole role) {
        gameEngine.setRole(role);
    }

    public void setConfigData(ConfigData configData) {
        gameEngine.setConfigData(configData);
    }

    public void onBecameMaster() {
        gameEngine.becomeMaster();
    }

    public NodeRole getCurrentRole() {
        return networkManager.getCurrentRole();
    }

    public GameState getGameState() {
        return gameEngine.getState();
    }

    public ConfigData getConfigData() {
        return gameEngine.getConfigData();
    }

    public int getPlayerID() {
        return networkManager.getPlayerID();
    }

    public void setPlayerRole(int playerId, NodeRole playerRole) {
        gameEngine.findPlayer(playerId).setNodeRole(playerRole);
    }

    public NodeRole getPlayerRole(int playerId) {
        GamePlayer player = gameEngine.findPlayer(playerId);
        if (player != null) {
            return player.getNodeRole();
        }
        return null;
    }

    public int getPort() {
        return networkManager.getPort();
    }

    public String getIpAddress() {
        return networkManager.getIpAddress();
    }
}