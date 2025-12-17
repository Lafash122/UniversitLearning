package app.main;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

import app.enums.*;
import app.gameStructures.*;
import app.utils.*;

public class GameEngine {
    private ConfigData configData;
    private GameState gameState;
    private boolean running;
    private ArrayList<GameStateListener> listeners = new ArrayList<>();
    private final Map<Integer, PendingSteer> pendingSteers = new ConcurrentHashMap<>();

    private Thread gameCycleThread;
    private int activePlayersCount;
    private NodeRole currentRole = NodeRole.VIEWER;
    private int playerID;

    private final GameCoordinator coordinator;

    public GameEngine(GameCoordinator coordinator) {
        this.coordinator = coordinator;
        gameState = new GameState(0);
        try {
            Properties properties = new Properties();
            InputStream inStream = GameEngine.class.getClassLoader().getResourceAsStream("config.properties");
            if (inStream == null) {
                throw new RuntimeException("config.properties не найден в classpath");
            }
            properties.load(inStream);
            configData = ConfigData.parse(properties);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (!ConfigValidator.validate(configData)) {
            throw new IllegalArgumentException();
        }
    }

    public boolean isMaster() {
        return currentRole == NodeRole.MASTER;
    }

    public void startGame(String playerName, boolean asMaster) {
        this.currentRole = asMaster ? NodeRole.MASTER : NodeRole.NORMAL;
        this.playerID = coordinator.getPlayerID();

        running = true;

        if (asMaster) {
            gameState.addPlayer(new GamePlayer(playerName, playerID, NodeRole.MASTER, PlayerType.HUMAN,
                                                coordinator.getPort(), coordinator.getIpAddress()));

            Snake mySnake = createSnakeForPlayer(playerID);
            if (mySnake != null) {
                gameState.addToSnakes(mySnake);
            }

            gameCycleThread = new Thread(this::gameCycle);
            gameCycleThread.start();
            notifyGameStarted();
        }
    }

    public void becomeMaster() {
        this.currentRole = NodeRole.MASTER;
        findPlayer(playerID).setNodeRole(NodeRole.MASTER);
        gameCycleThread = new Thread(this::gameCycle);
        gameCycleThread.start();
    }

    public boolean addNewPlayer(String name, int ID, NodeRole role, int port, String ipAddress) {
        if (role != NodeRole.NORMAL && role != NodeRole.VIEWER) {
            return false;
        }
        this.playerID = ID;
        GamePlayer newPlayer = new GamePlayer(name, playerID, role, PlayerType.HUMAN, port, ipAddress);
        gameState.addPlayer(newPlayer);

        if (role == NodeRole.NORMAL) {
            Snake newSnake = createSnakeForPlayer(playerID);
            if (newSnake != null) {
                gameState.addToSnakes(newSnake);
                return true;
            } else {
                gameState.removePlayer(playerID);
                activePlayersCount--;
                return false;
            }
        }

        return true;
    }

    private void gameCycle() {
        while (running) {
            try {
                long startTime = System.currentTimeMillis();

                updateGameState();
                notifyGameStateChanged();

                long elapsed = System.currentTimeMillis() - startTime;
                long sleepTime = configData.state_delay_ms() - elapsed;

                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {}
        }
    }

    public void stopGame() {
        running = false;
        if (gameCycleThread != null) {
            gameCycleThread.interrupt();
            try {
                gameCycleThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            gameCycleThread = null;
        }
        gameState = new GameState(0);
        activePlayersCount = 0;
        currentRole = NodeRole.VIEWER;
        notifyGameStopped();
    }

    private Snake createSnakeForPlayer(int playerID) {
        Snake newSnake = new Snake(playerID);
        Coord headCoord = findPlaceForHead();
        if (headCoord == null) {
            return null;
        }

        activePlayersCount++;
        gameState.getFoodList().remove(headCoord);

        newSnake.addCoord(headCoord);
        // Ищем место для тела
        for (int i = -1; i < 2; ++i) {
            for (int j = -1; j < 2; ++j) {
                if ((i == 0 && j != 0) || (i != 0 && j == 0)) {
                    Coord bodyCoord = new Coord(headCoord.x() + i, headCoord.y() + j);
                    if (!gameState.getFoodList().contains(bodyCoord)) {
                        newSnake.addCoord(new Coord(i, j));
                        if (i == 0 && j == 1) {
                            newSnake.setDirection(Direction.UP);
                        } else if (i == 0 && j == -1) {
                            newSnake.setDirection(Direction.DOWN);
                        } else if (i == 1 && j == 0) {
                            newSnake.setDirection(Direction.LEFT);
                        } else if (i == -1 && j == 0) {
                            newSnake.setDirection(Direction.RIGHT);
                        }
                        return newSnake;
                    }
                }
            }
        }

        return null;
    }

    private Coord findPlaceForHead() {
        int searchSize = 5;
        int radius = 2;

        Set<Coord> occupiedCells = getAllOccupiedCells();
        List<Coord> startPositions = new ArrayList<>();
        for (int y = 0; y < configData.height(); y++) {
            for (int x = 0; x < configData.width(); x++) {
                startPositions.add(new Coord(x, y));
            }
        }
        Collections.shuffle(startPositions);

        for (Coord startPos : startPositions) {
            if (isFreeSquare(startPos.x(), startPos.y(), occupiedCells, searchSize)) {
                return new Coord(
                        (startPos.x() + radius) % configData.width(),
                        (startPos.y() + radius) % configData.height()
                );
            }
        }
        return null;
    }

    private void updateGameState() {
        List<Snake> snakesToRemove = new ArrayList<>();
        applyPendingSteers();
        for (Snake snake : gameState.getSnakeList()) {
            ArrayList<Coord> snakeCoords = snake.getCoords();
            Coord direction = getDirectionCoord(snake.getDirection());
            Coord newHead = getNewCoord(snakeCoords.get(0), direction);

            if (checkCollision(newHead)) {
                snakesToRemove.add(snake);
            }
        }

        for (Snake snake : snakesToRemove) {
            killTheSnake(snake);
        }

        // Двигаем выживших змей
        for (Snake snake : gameState.getSnakeList()) {
            ArrayList<Coord> snakeCoords = snake.getCoords();
            Coord direction = getDirectionCoord(snake.getDirection());
            Coord newHead = getNewCoord(snakeCoords.get(0), direction);

            boolean ateFood = false;
            Iterator<Coord> foodIterator = gameState.getFoodList().iterator();
            while (foodIterator.hasNext()) {
                Coord food = foodIterator.next();
                if (newHead.equals(food)) {
                    GamePlayer player = findPlayer(snake.getPlayerID());
                    if (player != null) {
                        player.incrementScore();
                    }
                    foodIterator.remove();
                    ateFood = true;
                    break;
                }
            }

            snakeCoords.set(0, new Coord(-direction.x(), -direction.y()));

            snakeCoords.add(0, newHead);

            if (!ateFood && snakeCoords.size() > 1) {
                snakeCoords.remove(snakeCoords.size() - 1);
            }
        }

        for (int i = 0; i < activePlayersCount + configData.food_static() - gameState.getFoodList().size(); ++i) {
            addFood();
        }

        gameState.incrementGameState();
        coordinator.onGameStateUpdated(gameState);
    }

    public void updateFromNetwork(GameState state) {
        gameState = state;
    }

    private boolean checkCollision(Coord head) {
        for (Snake snake : gameState.getSnakeList()) {
            ArrayList<Coord> absCoords = getAbsoluteSnakeCoords(snake);
            for (Coord coord : absCoords) {
                if (head.equals(coord)) {
                    GamePlayer player = findPlayer(snake.getPlayerID());
                    if (player != null) {
                        player.incrementScore();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void addFood() {
        Set<Coord> occupiedCells = new HashSet<>();

        for (Snake snake : gameState.getSnakeList()) {
            occupiedCells.addAll(getAbsoluteSnakeCoords(snake));
        }

        occupiedCells.addAll(gameState.getFoodList());

        List<Coord> emptyCells = new ArrayList<>();
        for (int y = 0; y < configData.height(); y++) {
            for (int x = 0; x < configData.width(); x++) {
                Coord coord = new Coord(x, y);
                if (!occupiedCells.contains(coord)) {
                    emptyCells.add(coord);
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            Coord newFood = emptyCells.get((int)(Math.random() * emptyCells.size()));
            gameState.addToFood(newFood);
        }
    }

    public GamePlayer findPlayer(int ID) {
        for (GamePlayer player : gameState.getPlayersList()) {
            if (player.getID() == ID) {
                return player;
            }
        }
        return null;
    }

    private Snake findSnake(int ID) {
        for (Snake snake : gameState.getSnakeList()) {
            if (snake.getPlayerID() == ID) {
                return snake;
            }
        }
        return null;
    }

    private void killTheSnake(Snake snake) {
        gameState.getSnakeList().remove(snake);

        for (Coord coord : getAbsoluteSnakeCoords(snake)) {
            if (Math.random() >= 0.5) {
                gameState.getFoodList().add(coord);
            }
        }
        activePlayersCount--;
    }

    // Обработка накапливающихся сетевых steer
    private static class PendingSteer {
        Direction direction;
        long msgSeq;

        PendingSteer(Direction direction, long msgSeq) {
            this.direction = direction;
            this.msgSeq = msgSeq;
        }
    }

    public void addPendingSteer(int playerID, Direction direction, long msgSeq) {
        if (pendingSteers.containsKey(playerID)) {
            if (pendingSteers.get(playerID).msgSeq < msgSeq) {
                pendingSteers.put(playerID, new PendingSteer(direction, msgSeq));
            }
        } else {
            pendingSteers.put(playerID, new PendingSteer(direction, msgSeq));
        }
    }

    private void applyPendingSteers() {
        for (Map.Entry<Integer, PendingSteer> entry : pendingSteers.entrySet()) {
            setDirection(entry.getKey(), entry.getValue().direction);
        }
        pendingSteers.clear();
    }

    public void setDirection(int playerID, Direction direction) {
        Snake snake = findSnake(playerID);
        if (snake == null) {
            return;
        }
        Direction oldDirection = snake.getDirection();

        if (!((oldDirection == Direction.UP && direction == Direction.DOWN) ||
                (oldDirection == Direction.DOWN && direction == Direction.UP) ||
                (oldDirection == Direction.LEFT && direction == Direction.RIGHT) ||
                (oldDirection == Direction.RIGHT && direction == Direction.LEFT))) {
            snake.setDirection(direction);
        }
    }

    private ArrayList<Coord> getAbsoluteSnakeCoords(Snake snake) {
        ArrayList<Coord> newCoords = new ArrayList<>();
        ArrayList<Coord> snakeCoords = snake.getCoords();

        if (snakeCoords.isEmpty()) return newCoords;

        newCoords.add(snakeCoords.get(0));

        for (int i = 1; i < snakeCoords.size(); i++) {
            Coord prevAbs = newCoords.get(i - 1);
            Coord relOffset = snakeCoords.get(i);
            Coord nextAbs = getNewCoord(prevAbs, relOffset);
            newCoords.add(nextAbs);
        }

        return newCoords;
    }

    public void addGameStateListener(GameStateListener listener) {
        listeners.add(listener);
    }

    public void notifyGameStateChanged() {
        for (GameStateListener listener : listeners) {
            listener.onGameStateChanged(gameState);
        }
    }

    private void notifyGameStarted() {
        for (GameStateListener listener : listeners) {
            listener.onGameStarted();
        }
    }

    private void notifyGameStopped() {
        for (GameStateListener listener : listeners) {
            listener.onGameStopped();
        }
    }

    public void removePlayer(int playerID) {
        Snake snake = findSnake(playerID);
        if (snake != null) {
            snake.setSnakeState(SnakeState.ZOMBIE);
        }
        if (currentRole == NodeRole.MASTER) {
            gameState.removePlayer(playerID);
        }
    }

    // Вспомогательные методы
    private Coord getDirectionCoord(Direction direction) {
        return switch (direction) {
            case RIGHT -> new Coord(1, 0);
            case LEFT -> new Coord(-1, 0);
            case UP -> new Coord(0, -1);
            case DOWN -> new Coord(0, 1);
            default -> null;
        };
    }

    private Coord getNewCoord(Coord first, Coord second) {
        int newX = (first.x() + second.x() + configData.width()) % configData.width();
        int newY = (first.y() + second.y() + configData.height()) % configData.height();
        return new Coord(newX, newY);
    }

    private Set<Coord> getAllOccupiedCells() {
        Set<Coord> occupiedCells = new HashSet<>();
        for (Snake snake : gameState.getSnakeList()) {
            occupiedCells.addAll(getAbsoluteSnakeCoords(snake));
        }
        return occupiedCells;
    }

    private boolean isFreeSquare(int startX, int startY, Set<Coord> occupiedCells, int size) {
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                int x = (startX + dx) % configData.width();
                int y = (startY + dy) % configData.height();

                if (occupiedCells.contains(new Coord(x, y))) {
                    return false;
                }
            }
        }
        return true;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

    public void setRole(NodeRole role) {
        currentRole = role;
    }

    public GameState getState() {
        return gameState;
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public int getLastPlayerId() {
        return gameState.getPlayersList().get(gameState.getPlayersList().size() - 1).getID();
    }
}