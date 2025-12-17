package app.gameStructures;

import app.enums.NodeRole;
import app.enums.PlayerType;

public class GamePlayer {
    private String name;
    private int ID;
    private String ipAddress;
    private int port;
    private NodeRole nodeRole;
    private PlayerType playerType;
    private int score;

    public GamePlayer(String name, int ID, NodeRole nodeRole, PlayerType playerType, int port, String ipAddress) {
        this.name = name;
        this.ID = ID;
        this.nodeRole = nodeRole;
        this.playerType = playerType;
        this.score = 0;
        this.port = port;
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAdress) {
        this.ipAddress = ipAdress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public NodeRole getNodeRole() {
        return nodeRole;
    }

    public void setNodeRole(NodeRole nodeRole) {
        this.nodeRole = nodeRole;
    }

    public PlayerType getPlayerType() {
        return playerType;
    }

    public void setPlayerType(PlayerType playerType) {
        this.playerType = playerType;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void incrementScore() {
        ++this.score;
    }
}