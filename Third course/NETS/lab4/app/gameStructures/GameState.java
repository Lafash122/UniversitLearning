package app.gameStructures;

import java.util.ArrayList;

public class GameState {
    private int stateOrder;
    private ArrayList<Snake> snakeList;
    private ArrayList<Coord> foodList;
    private ArrayList<GamePlayer> playersList;

    public GameState(int stateOrder) {
        this.stateOrder = stateOrder;
        this.snakeList = new ArrayList<>();
        this.foodList = new ArrayList<>();
        this.playersList = new ArrayList<>();
    }

    public int getStateOrder() {
        return stateOrder;
    }

    public ArrayList<Snake> getSnakeList() {
        return snakeList;
    }

    public ArrayList<Coord> getFoodList() {
        return foodList;
    }

    public ArrayList<GamePlayer> getPlayersList() {
        return playersList;
    }

    public void setPlayersList(ArrayList<GamePlayer> playersList) {
        this.playersList = playersList;
    }

    public void setSnakeList(ArrayList<Snake> snakeList) {
        this.snakeList = snakeList;
    }

    public void addToSnakes(Snake snake) {
        snakeList.add(snake);
    }

    public void setFoodList(ArrayList<Coord> foodList) {
        this.foodList = foodList;
    }

    public void removeFromSnakes(int playerID) {
        snakeList.removeIf(snake -> snake.getPlayerID() == playerID);
    }

    public void addToFood(Coord coord) {
        foodList.add(coord);
    }

    public void removeFromFood(Coord coord) {
        foodList.removeIf(food -> (food.x() == coord.x() && food.y() == coord.y()));
    }

    public void addPlayer(GamePlayer player) {
        playersList.add(player);
    }

    public void removePlayer(int ID) {
        playersList.removeIf(player -> player.getID() == ID);
    }

    public void incrementGameState() {
        ++stateOrder;
    }
}