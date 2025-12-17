package app.gameStructures;

import app.enums.SnakeState;
import app.enums.Direction;

import java.util.ArrayList;

public class Snake {
    private SnakeState snakeState;
    private int playerID;
    private ArrayList<Coord> coords;
    private Direction direction;

    public Snake(int playerID) {
        this.snakeState = SnakeState.ALIVE;
        this.coords = new ArrayList<>();
        this.playerID = playerID;
        this.direction = Direction.DOWN;
    }

    public void setSnakeState(SnakeState snakeState) {
        this.snakeState = snakeState;
    }

    public void setPlayerID(int ID) {
        this.playerID = ID;
    }

    public void setCoords(ArrayList<Coord> coords) {
        this.coords = coords;
    }

    public void addCoord(Coord coord) {
        this.coords.add(coord);
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public SnakeState getSnakeState() {
        return snakeState;
    }

    public ArrayList<Coord> getCoords() {
        return coords;
    }

    public int getPlayerID() {
        return playerID;
    }

    public Direction getDirection() {
        return direction;
    }
}