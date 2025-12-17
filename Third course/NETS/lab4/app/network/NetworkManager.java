package app.network;

import app.gameStructures.ConfigData;
import app.gameStructures.Coord;
import app.gameStructures.Snake;
import app.snakeproto.SnakesProto;
import app.snakeproto.SnakesProto.*;

import app.enums.Direction;
import app.enums.NodeRole;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import app.gameStructures.GameState;
import app.main.GameCoordinator;

public class NetworkManager {
    private static final String MULTICAST_GROUP = "239.192.0.4";
    private static final int MULTICAST_PORT = 9192;
    private static final int TIME_INTERVAL_DIVIDER = 10;
    private static final long GAME_ANNOUNCEMENT_TIMEOUT = 1100;
    private int UNICAST_PORT;

    private MulticastSocket multicastSocket;
    private DatagramSocket unicastSocket;
    private InetAddress multicastGroup;

    private final GameCoordinator coordinator;

    private app.enums.NodeRole currentRole;
    private String gameName;
    private int playerID;

    private InetSocketAddress masterAddress;
    private InetSocketAddress deputyAddress;

    private final AtomicLong messageSequence = new AtomicLong(0);
    private final Map<Integer, InetSocketAddress> players = new ConcurrentHashMap<>();
    private final Map<Long, PendingMessage> pendingMessages = new ConcurrentHashMap<>();
    private final Map<String, GameAnnouncement> availableGames = new ConcurrentHashMap<>();
    private final Map<Integer, Long> lastSeen = new ConcurrentHashMap<>();

    private final ScheduledExecutorService connectionScheduler = Executors.newScheduledThreadPool(2);
    private final ScheduledExecutorService pingScheduler = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService gameWorkScheduler = Executors.newScheduledThreadPool(1);
    private final Thread unicastReceiverThread = new Thread(this::unicastReceiver);
    private final Thread multicastReceiverThread = new Thread(this::multicastReceiver);

    private Timer multicastAnnouncementTimer;

    private static class PendingMessage {
        GameMessage message;
        InetSocketAddress address;
        long timestamp;
        int retryCount;

        PendingMessage(GameMessage message, InetSocketAddress address) {
            this.message = message;
            this.address = address;
            this.timestamp = System.currentTimeMillis();
            this.retryCount = 0;
        }
    }

    private static class GameAnnouncement {
        String gameName;
        InetSocketAddress masterAddress;
        long lastSeen;
        SnakesProto.GameAnnouncement announcementData;

        GameAnnouncement(String gameName, InetSocketAddress masterAddress, SnakesProto.GameAnnouncement announcementData) {
            this.gameName = gameName;
            this.masterAddress = masterAddress;
            this.announcementData = announcementData;
            this.lastSeen = System.currentTimeMillis();
        }

        void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }
    }

    public NetworkManager(GameCoordinator coordinator) {
        this.coordinator = coordinator;
        try {
            multicastSocket = new MulticastSocket(MULTICAST_PORT);
            multicastSocket.setTimeToLive(16);
            multicastGroup = InetAddress.getByName(MULTICAST_GROUP);

            try {
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
                if (networkInterface != null) {
                    multicastSocket.joinGroup(new InetSocketAddress(multicastGroup, MULTICAST_PORT), networkInterface);
                } else {
                    multicastSocket.joinGroup(multicastGroup);
                }
            } catch (Exception e) {
                multicastSocket.joinGroup(multicastGroup);
            }

            unicastSocket = new DatagramSocket(0);
            UNICAST_PORT = unicastSocket.getLocalPort();

            startReceivers();
            startMaintenanceTasks();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    // Инициализация и запуск потоков
    private void startReceivers() {
        unicastReceiverThread.start();
        multicastReceiverThread.start();
    }

    private void startMaintenanceTasks() {
        connectionScheduler.scheduleAtFixedRate(this::cleanupOldGames, 2, 2, TimeUnit.SECONDS);
        connectionScheduler.scheduleAtFixedRate(this::checkNodesTimeout, 1, 1, TimeUnit.SECONDS);
        gameWorkScheduler.scheduleAtFixedRate(this::resendPendingMessages, 1, 1, TimeUnit.SECONDS);

        long pingSendRate = coordinator.getConfigData().state_delay_ms() / 20;
        pingScheduler.scheduleAtFixedRate(this::sendPingMessages, pingSendRate, pingSendRate, TimeUnit.MILLISECONDS);
    }

    // Управление игрой
    public void createGame(String gameName) {
        this.gameName = gameName;
        this.currentRole = app.enums.NodeRole.MASTER;
        this.playerID = generatePlayerID();

        try {
            InetAddress localAddress = InetAddress.getLocalHost();
            this.masterAddress = new InetSocketAddress(localAddress, UNICAST_PORT);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            this.masterAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), UNICAST_PORT);
        }
        players.put(playerID, this.masterAddress);
        startMulticastAnnouncements();
    }

    public void joinRoom(String roomName, String playerName) {
        GameAnnouncement game = availableGames.get(roomName);
        if (game != null) {
            this.gameName = roomName;
            this.masterAddress = game.masterAddress;
            this.currentRole = app.enums.NodeRole.NORMAL;
            this.playerID = generatePlayerID();

            app.gameStructures.ConfigData configData = new ConfigData(
                    game.announcementData.getConfig().getWidth(),
                    game.announcementData.getConfig().getHeight(),
                    game.announcementData.getConfig().getFoodStatic(),
                    game.announcementData.getConfig().getStateDelayMs()
            );

            coordinator.setConfigData(configData);
            sendJoinMessage(this.masterAddress, playerName, gameName, app.enums.NodeRole.NORMAL);
            try {
                players.put(playerID, new InetSocketAddress(InetAddress.getLocalHost(), UNICAST_PORT));
            } catch (UnknownHostException e) {}
        }
    }

    public void disconnect() {
        if (currentRole == NodeRole.MASTER) {
            stopMulticastAnnouncements();
        }
        unicastSocket.close();
        multicastSocket.close();
        unicastReceiverThread.interrupt();
        multicastReceiverThread.interrupt();
        gameWorkScheduler.shutdown();
        pingScheduler.shutdown();
        connectionScheduler.shutdown();
    }

    // Heartbeat и управление соединениями
    private void updateLastSeen(int nodeId) {
        lastSeen.put(nodeId, System.currentTimeMillis());
    }

    private boolean isNodeAlive(int nodeId) {
        Long lastSeenTime = lastSeen.get(nodeId);
        if (lastSeenTime == null) { return false; }
        return System.currentTimeMillis() - lastSeenTime < getAliveTimeout();
    }

    private long getAliveTimeout() {
        return (long)(coordinator.getConfigData().state_delay_ms() * 0.8);
    }

    private void checkNodesTimeout() {
        if (currentRole == NodeRole.MASTER && deputyAddress == null && !(players.keySet().size() == 1)) {
            selectNewDeputy();
        }

        List<Integer> deadNodes = new ArrayList<>();
        for (Map.Entry<Integer, InetSocketAddress> entry : players.entrySet()) {
            int nodeId = entry.getKey();
            if (nodeId == this.playerID) { continue; }
            if (!isNodeAlive(nodeId)) {
                deadNodes.add(nodeId);
                handleDeadNode(nodeId);
            }
        }
        for (int deadNodeId : deadNodes) {
            lastSeen.remove(deadNodeId);
            players.remove(deadNodeId);
            coordinator.onPlayerDisconnected(deadNodeId);
        }
    }

    private void handleDeadNode(int nodeId) {
        NodeRole deadNodeRole = coordinator.getPlayerRole(nodeId);

        if (currentRole == NodeRole.NORMAL && deadNodeRole == NodeRole.MASTER) {
            updatePendingMessagesForNewMaster(this.masterAddress, this.deputyAddress);
            this.masterAddress = this.deputyAddress;
            deputyAddress = null;
        } else if (currentRole == NodeRole.MASTER && deadNodeRole == NodeRole.DEPUTY) {
            deputyAddress = null;
        } else if (currentRole == NodeRole.DEPUTY && deadNodeRole == NodeRole.MASTER) {
            coordinator.setPlayerRole(playerID, NodeRole.MASTER);
            this.currentRole = NodeRole.MASTER;
            this.masterAddress = this.deputyAddress;
            this.deputyAddress = null;
            coordinator.onBecameMaster();
            startMulticastAnnouncements();
        }
        players.remove(nodeId);
    }

    private void selectNewDeputy() {
        for (Map.Entry<Integer, InetSocketAddress> player : players.entrySet()) {
            int nodeId = player.getKey();
            if (nodeId == this.playerID) { continue; }
            if (coordinator.getPlayerRole(nodeId) == app.enums.NodeRole.NORMAL) {
                deputyAddress = player.getValue();
            }
            sendRoleChangeMessage(deputyAddress, nodeId, NodeRole.MASTER, NodeRole.DEPUTY);
            coordinator.setPlayerRole(nodeId, NodeRole.DEPUTY);
            return;
        }
        System.err.println("No suitable player found for DEPUTY role");
        deputyAddress = null;
    }

    private void updatePendingMessagesForNewMaster(InetSocketAddress oldMaster, InetSocketAddress newMaster) {
        for (PendingMessage pending : pendingMessages.values()) {
            if (pending.address.equals(oldMaster)) {
                pending.address = newMaster;
            }
        }
    }

    // Прием сообщений
    private void unicastReceiver() {
        byte[] buffer = new byte[8192];
        while (!unicastSocket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                unicastSocket.receive(packet);
                GameMessage message = GameMessage.parseFrom(Arrays.copyOf(packet.getData(), packet.getLength()));
                processMessage(message, packet.getAddress(), packet.getPort());
            } catch (IOException e) {
                if (!unicastSocket.isClosed()) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void multicastReceiver() {
        byte[] buffer = new byte[8192];
        while (!multicastSocket.isClosed()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);
                GameMessage message = GameMessage.parseFrom(Arrays.copyOf(packet.getData(), packet.getLength()));
                processMessage(message, packet.getAddress(), packet.getPort());
            } catch (IOException e) {
                if (!multicastSocket.isClosed()) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void processMessage(GameMessage message, InetAddress address, int port) {
        InetSocketAddress socketAddress = new InetSocketAddress(address, port);

        if (message.hasSenderId()) {
            updateLastSeen(message.getSenderId());
        }

        switch (message.getTypeCase()) {
            case ACK -> processAckMessage(message, socketAddress);
            case JOIN -> processJoinMessage(message, socketAddress);
            case PING -> processPingMessage(message, socketAddress);
            case ERROR -> processErrorMessage(message, socketAddress);
            case STATE -> processStateMessage(message, socketAddress);
            case STEER -> processSteerMessage(message, socketAddress);
            case ROLE_CHANGE -> processRoleChangeMessage(message, socketAddress);
            case ANNOUNCEMENT -> processAnnouncementMessage(message, socketAddress);
            case DISCOVER -> processDiscoverMessage(message, socketAddress);
        }
    }

    // Отправка сообщений
    public void broadcastGameState(app.gameStructures.GameState state) {
        if (currentRole == NodeRole.MASTER) {
            SnakesProto.GameState protoState = convertToProtoState(state);
            for (Map.Entry<Integer, InetSocketAddress> entry : players.entrySet()) {
                int nodeId = entry.getKey();
                if (nodeId == this.playerID) { continue; }
                sendStateMessage(players.get(nodeId), protoState);
            }
        }
    }

    private SnakesProto.GameState convertToProtoState(app.gameStructures.GameState state) {
        SnakesProto.GameState.Builder builder = SnakesProto.GameState.newBuilder()
                .setStateOrder(state.getStateOrder());

        for (app.gameStructures.Coord food : state.getFoodList()) {
            builder.addFoods(
                    SnakesProto.GameState.Coord.newBuilder()
                            .setX(food.x())
                            .setY(food.y())
                            .build()
            );
        }

        for (app.gameStructures.Snake snake : state.getSnakeList()) {
            SnakesProto.GameState.Snake.Builder snakeBuilder = SnakesProto.GameState.Snake.newBuilder()
                    .setPlayerId(snake.getPlayerID())
                    .setState(SnakesProto.GameState.Snake.SnakeState.valueOf(snake.getSnakeState().name()))
                    .setHeadDirection(SnakesProto.Direction.valueOf(snake.getDirection().name()));

            for (app.gameStructures.Coord coord : snake.getCoords()) {
                snakeBuilder.addPoints(
                        SnakesProto.GameState.Coord.newBuilder()
                                .setX(coord.x())
                                .setY(coord.y())
                                .build()
                );
            }
            builder.addSnakes(snakeBuilder);
        }

        SnakesProto.GamePlayers.Builder playersBuilder = SnakesProto.GamePlayers.newBuilder();
        for (app.gameStructures.GamePlayer player : state.getPlayersList()) {
            playersBuilder.addPlayers(
                    SnakesProto.GamePlayer.newBuilder()
                            .setName(player.getName())
                            .setId(player.getID())
                            .setIpAddress(player.getIpAddress())
                            .setPort(player.getPort())
                            .setRole(SnakesProto.NodeRole.valueOf(player.getNodeRole().name()))
                            .setType(SnakesProto.PlayerType.valueOf(player.getPlayerType().name()))
                            .setScore(player.getScore())
                            .build()
            );
        }
        builder.setPlayers(playersBuilder.build());
        return builder.build();
    }

    private void sendStateMessage(InetSocketAddress address, SnakesProto.GameState state) {
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(messageSequence.incrementAndGet())
                .setSenderId(playerID)
                .setReceiverId(-1)
                .setState(GameMessage.StateMsg.newBuilder()
                        .setState(state)
                        .build()
                );
        GameMessage stateMsg = builder.build();
        sendUnicastMessage(stateMsg, address, true);
    }

    private void sendAnnouncementMessage(InetSocketAddress address) {
        if (currentRole == NodeRole.MASTER) {
            SnakesProto.GameAnnouncement announcement = createGameAnnouncement();

            GameMessage.AnnouncementMsg announcementMsg = GameMessage.AnnouncementMsg.newBuilder()
                    .addGames(announcement)
                    .build();

            GameMessage.Builder builder = GameMessage.newBuilder()
                    .setMsgSeq(messageSequence.incrementAndGet())
                    .setAnnouncement(announcementMsg);

            GameMessage announcementMessage = builder.build();

            if (address != null) {
                sendUnicastMessage(announcementMessage, address, false);
            } else {
                sendMulticastMessage(announcementMessage);
            }
        }
    }

    private SnakesProto.GameAnnouncement createGameAnnouncement() {
        app.gameStructures.GameState currentState = coordinator.getGameState();
        SnakesProto.GamePlayers.Builder playersBuilder = SnakesProto.GamePlayers.newBuilder();

        for (app.gameStructures.GamePlayer player : currentState.getPlayersList()) {
            playersBuilder.addPlayers(
                    SnakesProto.GamePlayer.newBuilder()
                            .setName(player.getName())
                            .setId(player.getID())
                            .setPort(player.getPort())
                            .setIpAddress(player.getIpAddress())
                            .setRole(SnakesProto.NodeRole.valueOf(player.getNodeRole().name()))
                            .setType(SnakesProto.PlayerType.valueOf(player.getPlayerType().name()))
                            .setScore(player.getScore())
                            .build()
            );
        }

        app.gameStructures.ConfigData config = coordinator.getConfigData();
        SnakesProto.GameConfig configProto = SnakesProto.GameConfig.newBuilder()
                .setWidth(config.width())
                .setHeight(config.height())
                .setFoodStatic(config.food_static())
                .setStateDelayMs(config.state_delay_ms())
                .build();

        boolean canJoin = canPlayerJoin();

        return SnakesProto.GameAnnouncement.newBuilder()
                .setPlayers(playersBuilder.build())
                .setConfig(configProto)
                .setCanJoin(canJoin)
                .setGameName(gameName)
                .build();
    }

    private boolean canPlayerJoin() {
        return coordinator.getGameState().getSnakeList().size() < 10;
    }

    private void sendDiscoverMessage() {
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(messageSequence.incrementAndGet())
                .setDiscover(GameMessage.DiscoverMsg.newBuilder().build());
        GameMessage discover = builder.build();
        sendMulticastMessage(discover);
    }

    private void sendAckMessage(InetSocketAddress address, long msgSeq) {
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(msgSeq)
                .setAck(GameMessage.AckMsg.newBuilder().build());
        builder.setSenderId(playerID);
        GameMessage ack = builder.build();
        sendUnicastMessage(ack, address, false);
    }

    private void sendJoinMessage(InetSocketAddress address, String playerName, String gameName, NodeRole role) {
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(messageSequence.incrementAndGet())
                .setJoin(GameMessage.JoinMsg.newBuilder()
                        .setPlayerName(playerName)
                        .setGameName(gameName)
                        .setRequestedRole(SnakesProto.NodeRole.valueOf(role.name())).build());
        builder.setSenderId(playerID);
        GameMessage join = builder.build();
        sendUnicastMessage(join, address, true);
    }

    private void sendPingMessage(InetSocketAddress address) {
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(messageSequence.incrementAndGet())
                .setPing(GameMessage.PingMsg.newBuilder().build());
        builder.setSenderId(playerID);
        GameMessage ping = builder.build();
        sendUnicastMessage(ping, address, true);
    }

    public void sendErrorMessage(InetSocketAddress address, String errorMessage) {
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(messageSequence.incrementAndGet())
                .setError(GameMessage.ErrorMsg.newBuilder()
                        .setErrorMessage(errorMessage).build());
        builder.setSenderId(playerID);
        GameMessage error = builder.build();
        sendUnicastMessage(error, address, true);
    }

    private void sendRoleChangeMessage(InetSocketAddress address, int receiverId, NodeRole senderRole, NodeRole receiverRole) {
        GameMessage.RoleChangeMsg.Builder roleChangeBuilder = GameMessage.RoleChangeMsg.newBuilder();
        if (senderRole != null) { roleChangeBuilder.setSenderRole(SnakesProto.NodeRole.valueOf(senderRole.name())); }
        if (receiverRole != null) { roleChangeBuilder.setReceiverRole(SnakesProto.NodeRole.valueOf(receiverRole.name())); }
        GameMessage.Builder builder = GameMessage.newBuilder()
                .setMsgSeq(messageSequence.incrementAndGet())
                .setSenderId(playerID)
                .setReceiverId(receiverId)
                .setRoleChange(roleChangeBuilder.build());

        GameMessage roleChange = builder.build();
        sendUnicastMessage(roleChange, address, true);
    }

    public void sendSteerMessage(Direction direction) {
        if (masterAddress != null && (currentRole == app.enums.NodeRole.NORMAL || currentRole == app.enums.NodeRole.DEPUTY)) {
            GameMessage.Builder builder = GameMessage.newBuilder()
                    .setMsgSeq(messageSequence.incrementAndGet())
                    .setSenderId(playerID)
                    .setReceiverId(-1)
                    .setSteer(GameMessage.SteerMsg.newBuilder()
                            .setDirection(SnakesProto.Direction.valueOf(direction.name())).build());

            GameMessage steer = builder.build();
            sendUnicastMessage(steer, masterAddress, true);
        }
    }

    private void sendUnicastMessage(GameMessage message, InetSocketAddress address, boolean requireAck) {
        if (address == null) {
            System.err.println("Cannot send message: address is null " + message.getTypeCase().name());
            return;
        }
        try {
            byte[] data = message.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, address);
            unicastSocket.send(packet);

            if (requireAck) {
                pendingMessages.put(message.getMsgSeq(), new PendingMessage(message, address));
            }

        } catch (IOException e) {
            System.err.println("Failed to send unicast message: " + e.getMessage());
        }
    }

    private void sendMulticastMessage(GameMessage message) {
        try {
            byte[] data = message.toByteArray();
            DatagramPacket packet = new DatagramPacket(data, data.length, multicastGroup, MULTICAST_PORT);
            multicastSocket.send(packet);
        } catch (IOException e) {
            System.err.println("Failed to send multicast message: " + e.getMessage());
        }
    }

    // Управление multicast анонсами
    private void startMulticastAnnouncements() {
        if (multicastAnnouncementTimer != null) {
            multicastAnnouncementTimer.cancel();
        }
        multicastAnnouncementTimer = new Timer("MulticastAnnouncer");
        multicastAnnouncementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendAnnouncementMessage(null);
            }
        }, 0, 1000);
    }

    private void stopMulticastAnnouncements() {
        if (multicastAnnouncementTimer != null) {
            multicastAnnouncementTimer.cancel();
            multicastAnnouncementTimer = null;
        }
    }

    // Технические задачи обслуживания
    private void resendPendingMessages() {
        long now = System.currentTimeMillis();
        long resendDelay = coordinator.getGameEngine().getConfigData().state_delay_ms() / TIME_INTERVAL_DIVIDER;

        for (PendingMessage pending : pendingMessages.values()) {
            if (now - pending.timestamp > resendDelay) {
                if (pending.retryCount < 5) {
                    sendUnicastMessage(pending.message, pending.address, false);
                    pending.timestamp = now;
                    pending.retryCount++;
                } else {
                    pendingMessages.remove(pending.message.getMsgSeq());
                }
            }
        }
    }

    private void sendPingMessages() {
        for (Map.Entry<Integer, InetSocketAddress> entry : players.entrySet()) {
            int nodeId = entry.getKey();
            if (nodeId == this.playerID) { continue; }
            sendPingMessage(players.get(nodeId));
        }
    }

    private void cleanupOldGames() {
        long now = System.currentTimeMillis();
        availableGames.entrySet().removeIf(entry -> now - entry.getValue().lastSeen > GAME_ANNOUNCEMENT_TIMEOUT);
    }

    // Обработка входящих сообщений
    private void processAckMessage(GameMessage message, InetSocketAddress address) {
        pendingMessages.remove(message.getMsgSeq());
    }

    private void processJoinMessage(GameMessage message, InetSocketAddress address) {
        if (currentRole == app.enums.NodeRole.MASTER) {
            String playerName = message.getJoin().getPlayerName();
            String joinGameName = message.getJoin().getGameName();
            app.enums.NodeRole requestedRole = app.enums.NodeRole.valueOf(message.getJoin().getRequestedRole().name());
            if (joinGameName.equals(this.gameName)) {
                sendAckMessage(address, message.getMsgSeq());
                coordinator.onPlayerJoined(playerName, address, message.getSenderId(), requestedRole);
            } else {
                sendErrorMessage(address, "Game not found");
            }
        }
    }

    private void processPingMessage(GameMessage message, InetSocketAddress address) {
        if (!players.containsKey(message.getSenderId())) {
            players.put(message.getSenderId(), address);
        }
        sendAckMessage(address, message.getMsgSeq());
    }

    private void processErrorMessage(GameMessage message, InetSocketAddress address) {
        System.err.println("Received error: " + message.getError().getErrorMessage());
    }

    private void processStateMessage(GameMessage message, InetSocketAddress address) {
        SnakesProto.GameState gameState = message.getState().getState();
        app.gameStructures.GameState newState = new GameState(gameState.getStateOrder());

        List<SnakesProto.GameState.Coord> gameFoodList = gameState.getFoodsList();
        ArrayList<app.gameStructures.Coord> newFoodList = new ArrayList<>();
        for (SnakesProto.GameState.Coord coord : gameFoodList) {
            newFoodList.add(new Coord(coord.getX(), coord.getY()));
        }
        newState.setFoodList(newFoodList);

        List<SnakesProto.GameState.Snake> gameSnakeList = gameState.getSnakesList();
        ArrayList<app.gameStructures.Snake> newSnakeList = new ArrayList<>();
        for (SnakesProto.GameState.Snake snake : gameSnakeList) {
            app.gameStructures.Snake newSnake = new Snake(snake.getPlayerId());
            for (SnakesProto.GameState.Coord coord : snake.getPointsList()) {
                newSnake.addCoord(new Coord(coord.getX(), coord.getY()));
            }
            newSnake.setDirection(app.enums.Direction.valueOf(snake.getHeadDirection().name()));
            newSnake.setSnakeState(app.enums.SnakeState.valueOf(snake.getState().name()));
            newSnakeList.add(newSnake);
        }
        newState.setSnakeList(newSnakeList);

        List<SnakesProto.GamePlayer> gamePlayers = gameState.getPlayers().getPlayersList();
        ArrayList<app.gameStructures.GamePlayer> newPlayers = new ArrayList<>();
        for (SnakesProto.GamePlayer player : gamePlayers) {
            app.gameStructures.GamePlayer newPlayer = new app.gameStructures.GamePlayer(
                    player.getName(), player.getId(),
                    app.enums.NodeRole.valueOf(player.getRole().name()),
                    app.enums.PlayerType.valueOf(player.getType().name()),
                    player.getPort(),
                    player.getIpAddress()
            );
            newPlayer.setScore(player.getScore());
            newPlayers.add(newPlayer);

            if (!players.containsKey(newPlayer.getID()) && newPlayer.getID() != playerID) {
                players.put(newPlayer.getID(), new InetSocketAddress(newPlayer.getIpAddress(), newPlayer.getPort()));
            }

            if (newPlayer.getNodeRole() == NodeRole.DEPUTY) {
                this.deputyAddress = players.get(newPlayer.getID());
            }

            if (newPlayer.getNodeRole() == NodeRole.MASTER) {
                this.masterAddress = players.get(newPlayer.getID());
            }
        }
        newState.setPlayersList(newPlayers);

        sendAckMessage(address, message.getMsgSeq());
        coordinator.onGameStateReceived(newState);
    }

    private void processSteerMessage(GameMessage message, InetSocketAddress address) {
        if (currentRole == app.enums.NodeRole.MASTER) {
            int senderId = message.getSenderId();
            app.enums.Direction direction = app.enums.Direction.valueOf(message.getSteer().getDirection().name());
            coordinator.onSteerCommandReceived(senderId, direction, message.getMsgSeq());
        }
    }

    private void processRoleChangeMessage(GameMessage message, InetSocketAddress address) {
        if (!message.getRoleChange().hasReceiverRole()) {
            return;
        }
        app.enums.NodeRole receiverRole = app.enums.NodeRole.valueOf(message.getRoleChange().getReceiverRole().name());
        int receiverId = message.getReceiverId();

        if (receiverId == this.playerID) {
            if (receiverRole == NodeRole.VIEWER) {
                currentRole = NodeRole.VIEWER;
            } else if (receiverRole == NodeRole.DEPUTY && currentRole == NodeRole.NORMAL) {
                currentRole = NodeRole.DEPUTY;
                try {
                    this.deputyAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), UNICAST_PORT);
                } catch (UnknownHostException e) {}
            } else if (receiverRole == NodeRole.MASTER && currentRole == NodeRole.DEPUTY) {
                currentRole = NodeRole.MASTER;
                this.deputyAddress = null;
                try {
                    this.masterAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), UNICAST_PORT);
                } catch (UnknownHostException e) {}
                startMulticastAnnouncements();
            }
            coordinator.onRoleChanged(receiverRole);
        }
    }

    private void processAnnouncementMessage(GameMessage message, InetSocketAddress address) {
        SnakesProto.GameAnnouncement announcement = message.getAnnouncement().getGames(0);
        String announcedGameName = announcement.getGameName();
        GameAnnouncement existingAnnouncement = availableGames.get(announcedGameName);
        if (existingAnnouncement != null) {
            existingAnnouncement.updateLastSeen();
            existingAnnouncement.announcementData = announcement;
        } else {
            availableGames.put(announcedGameName,
                    new GameAnnouncement(announcedGameName, address, announcement));
        }
    }

    private void processDiscoverMessage(GameMessage message, InetSocketAddress address) {
        if (currentRole == NodeRole.MASTER) {
            sendAnnouncementMessage(address);
        }
    }

    // Вспомогательные методы
    private int generatePlayerID() {
        return Math.abs(UUID.randomUUID().hashCode() % 1000000);
    }

    public void addPlayer(int playerId, InetSocketAddress address) {
        players.put(playerId, address);
    }

    public List<String> discoverGames() {
        sendDiscoverMessage();
        return new ArrayList<>(availableGames.keySet());
    }

    public ArrayList<String> getRoomsList() {
        return new ArrayList<>(availableGames.keySet());
    }

    public NodeRole getCurrentRole() {
        return currentRole;
    }

    public int getPlayerID() {
        return playerID;
    }

    public int getPort() {
        return UNICAST_PORT;
    }

    public String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}