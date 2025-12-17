package app.main;

import app.enums.Direction;
import app.enums.NodeRole;
import app.gameStructures.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UIManager implements GameStateListener {
    private JFrame menuFrame;
    private JFrame gameFrame;
    private JPanel mainMenuPanel;
    private Field gameField;
    private JPanel playersPanel;

    private GameCoordinator coordinator;
    private KeyAdapter inputListener;

    // Константы для цветов
    private static final Color BACKGROUND_COLOR = new Color(0x34a0a4);
    private static final Color TEXT_COLOR = new Color(0xd9ed92);
    private static final Color BUTTON_COLOR = new Color(0x1e6091);
    private static final Color GRID_COLOR = new Color(0x184e77);
    private static final Color SNAKE_HEAD_COLOR = new Color(0xd9ed92);
    private static final Color SNAKE_BODY_COLOR = new Color(0x99d98c);
    private static final Color FOOD_COLOR = new Color(0xc9184a);

    // Константы для шрифтов
    private static final Font MAIN_FONT = new Font("Comic Sans MS", Font.BOLD + Font.ITALIC, 57);
    private static final Font TITLE_FONT = new Font("Comic Sans MS", Font.BOLD, 30);
    private static final Font INPUT_FONT = new Font("Comic Sans MS", Font.BOLD, 25);
    private static final Font BUTTON_FONT = new Font("Comic Sans MS", Font.BOLD, 16);
    private static final Font PLAYER_NAME_FONT = new Font("Comic Sans MS", Font.BOLD, 20);
    private static final Font PLAYER_SCORE_FONT = new Font("Comic Sans MS", Font.BOLD, 20);

    // Константы для размеров
    private static final int MAIN_FRAME_SIZE = 700;
    private static final int GAME_FRAME_WIDTH = 1050;
    private static final int GAME_FRAME_HEIGHT = 700;
    private static final int FIELD_WIDTH = 700;
    private static final int FIELD_HEIGHT = 650;
    private static final int INFO_PANEL_WIDTH = 300;
    private static final int INFO_PANEL_HEIGHT = 650;

    // Размеры кнопок
    private static final Dimension STANDARD_BUTTON_SIZE = new Dimension(200, 50);
    private static final Dimension ALT_BUTTON_SIZE = new Dimension(200, 150);

    // Отступы
    private static final int MAIN_MENU_BORDER = 50;
    private static final int BUTTON_PANEL_BORDER = 20;
    private static final int INPUT_FIELD_WIDTH = 300;
    private static final int INPUT_FIELD_HEIGHT = 40;
    private static final int LABEL_WIDTH = 300;
    private static final int LABEL_HEIGHT = 30;

    public UIManager(GameCoordinator coordinator) {
        this.coordinator = coordinator;
        coordinator.getGameEngine().addGameStateListener(this);
        initializeMenuFrame();
    }

    private void initializeMenuFrame() {
        menuFrame = new JFrame("Змейка");
        mainMenuPanel = new JPanel();
        menuFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        menuFrame.setSize(MAIN_FRAME_SIZE, MAIN_FRAME_SIZE);
        menuFrame.setResizable(false);
        menuFrame.setLocationRelativeTo(null);
        menuFrame.getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void setupKeyListener() {
        if (inputListener != null) {
            gameFrame.removeKeyListener(inputListener);
        }

        inputListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }
        };

        gameFrame.addKeyListener(inputListener);
        gameFrame.setFocusable(true);
        gameFrame.requestFocusInWindow();
    }

    private void handleKeyPress(KeyEvent e) {
        NodeRole role = coordinator.getCurrentRole();

        if (role == NodeRole.MASTER) {
            handleMasterKeyPress(e);
        } else if (role != NodeRole.VIEWER) {
            handleNonViewerKeyPress(e);
        }
    }

    private void handleMasterKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> coordinator.steerFromInput(Direction.UP);
            case KeyEvent.VK_S -> coordinator.steerFromInput(Direction.DOWN);
            case KeyEvent.VK_A -> coordinator.steerFromInput(Direction.LEFT);
            case KeyEvent.VK_D -> coordinator.steerFromInput(Direction.RIGHT);
        }
    }

    private void handleNonViewerKeyPress(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> coordinator.sendSteer(Direction.UP);
            case KeyEvent.VK_S -> coordinator.sendSteer(Direction.DOWN);
            case KeyEvent.VK_A -> coordinator.sendSteer(Direction.LEFT);
            case KeyEvent.VK_D -> coordinator.sendSteer(Direction.RIGHT);
        }
    }

    @Override
    public void onGameStateChanged(GameState newState) {
        SwingUtilities.invokeLater(() -> {
            if (gameField != null) {
                gameField.redraw(newState);
            }
            updatePlayersPanel(newState);
        });
    }

    @Override
    public void onGameStarted() {
    }

    @Override
    public void onGameStopped() {
        if (inputListener != null) {
            gameFrame.removeKeyListener(inputListener);
        }
    }

    public void start() {
        showMainMenu();
    }

    public void showMainMenu() {
        menuFrame.setVisible(true);
        setupMainMenuLayout();
        setupMainMenuComponents();
    }

    private void setupMainMenuLayout() {
        mainMenuPanel.setLayout(new GridLayout(2, 1, 0, 15));
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(MAIN_MENU_BORDER, 180, 100, 180));
        mainMenuPanel.setBackground(BACKGROUND_COLOR);
    }

    private void setupMainMenuComponents() {
        mainMenuPanel.removeAll();

        JLabel nameLabel = createTitleLabel("Snake game");
        JPanel buttonPanel = createMainMenuButtonPanel();

        mainMenuPanel.add(nameLabel);
        mainMenuPanel.add(buttonPanel);
        mainMenuPanel.updateUI();
        menuFrame.add(mainMenuPanel);
        menuFrame.setVisible(true);
    }

    private JPanel createMainMenuButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 0, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, BUTTON_PANEL_BORDER, 0, BUTTON_PANEL_BORDER));
        buttonPanel.setBackground(BACKGROUND_COLOR);

        JButton createGameButton = createButton("Создать", e -> showCreateInfoMenu());
        JButton joinGameButton = createButton("Присоединиться", e -> showJoinMenu());
        JButton exitButton = createButton("Выход", e -> {
            coordinator.abortGame();
            System.exit(0);
        });

        buttonPanel.add(createGameButton);
        buttonPanel.add(joinGameButton);
        buttonPanel.add(exitButton);

        return buttonPanel;
    }

    private void showCreateInfoMenu() {
        mainMenuPanel.removeAll();
        setupFormLayout();
        setupCreateInfoMenuComponents();
    }

    private void setupFormLayout() {
        mainMenuPanel.setLayout(null);
        mainMenuPanel.setBackground(BACKGROUND_COLOR);
    }

    private void setupCreateInfoMenuComponents() {
        JLabel gameNameLabel = createInputLabel("Введите название:", 185, 50);
        JTextField gameNameField = createInputField(180, 110);

        JLabel playerNameLabel = createInputLabel("Введите ваше имя:", 185, 170);
        JTextField playerNameField = createInputField(180, 220);

        JButton doneButton = createButton("Готово", 180, 500, e -> {
            handleCreateGame(gameNameField.getText(), playerNameField.getText());
        });

        JButton backButton = createButton("Назад", 180, 570, e -> showMainMenu());

        addComponentsToPanel(gameNameLabel, gameNameField, playerNameLabel, playerNameField, doneButton, backButton);
    }

    private void handleCreateGame(String gameName, String playerName) {
        if (!playerName.isEmpty() && !gameName.isEmpty()) {
            coordinator.createNewGame(gameName, playerName);
        }
    }

    private void showJoinMenu() {
        mainMenuPanel.removeAll();
        setupJoinMenuLayout();
        setupJoinMenuComponents();
    }

    private void setupJoinMenuLayout() {
        mainMenuPanel.setLayout(new BoxLayout(mainMenuPanel, BoxLayout.X_AXIS));
        mainMenuPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    }

    private void setupJoinMenuComponents() {
        JPanel leftPanel = createJoinMenuLeftPanel();
        JPanel rightPanel = createJoinMenuRightPanel();

        mainMenuPanel.add(leftPanel);
        mainMenuPanel.add(rightPanel);
        mainMenuPanel.updateUI();
    }

    private JPanel createJoinMenuLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(BACKGROUND_COLOR);
        leftPanel.setMaximumSize(new Dimension(320, Integer.MAX_VALUE));

        JLabel joinMenuLabel = createJoinMenuLabel();
        leftPanel.add(joinMenuLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel joinButtonPanel = createJoinButtonPanel();
        JScrollPane scrollPane = createJoinMenuScrollPane(joinButtonPanel);
        leftPanel.add(scrollPane);

        return leftPanel;
    }

    private JPanel createJoinMenuRightPanel() {
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BACKGROUND_COLOR);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 0));

        JButton backButton = createJoinMenuButton("Назад", ALT_BUTTON_SIZE, e -> showMainMenu());
        JButton updateButton = createJoinMenuButton("Обновить", ALT_BUTTON_SIZE, e -> showJoinMenu());

        rightPanel.add(backButton);
        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(updateButton);

        return rightPanel;
    }

    private JLabel createJoinMenuLabel() {
        JLabel label = new JLabel("Комнаты: ");
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JPanel createJoinButtonPanel() {
        JPanel joinButtonPanel = new JPanel();
        joinButtonPanel.setLayout(new BoxLayout(joinButtonPanel, BoxLayout.Y_AXIS));
        joinButtonPanel.setBackground(BACKGROUND_COLOR);

        List<String> roomsList = coordinator.getAvailableGames();

        for (int i = 0; i < roomsList.size(); i++) {
            String roomName = roomsList.get(i);
            JButton roomButton = createRoomButton(roomName);
            joinButtonPanel.add(roomButton);

            if (i < roomsList.size() - 1) {
                joinButtonPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        return joinButtonPanel;
    }

    private JButton createRoomButton(String roomName) {
        JButton button = createButton(roomName, STANDARD_BUTTON_SIZE, e -> showJoinInfoMenu(roomName));
        button.setPreferredSize(STANDARD_BUTTON_SIZE);
        button.setMaximumSize(STANDARD_BUTTON_SIZE);
        button.setMinimumSize(STANDARD_BUTTON_SIZE);
        return button;
    }

    private JButton createJoinMenuButton(String text, Dimension size, java.awt.event.ActionListener listener) {
        JButton button = createButton(text, size, listener);
        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return button;
    }

    private JScrollPane createJoinMenuScrollPane(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        return scrollPane;
    }

    private void showJoinInfoMenu(String roomName) {
        mainMenuPanel.removeAll();
        setupFormLayout();
        setupJoinInfoMenuComponents(roomName);
    }

    private void setupJoinInfoMenuComponents(String roomName) {
        JLabel playerNameLabel = createInputLabel("Введите ваше имя:", 185, 50);
        JTextField playerNameField = createInputField(180, 110);

        JButton doneButton = createButton("Готово", 180, 500, e -> {
            handleJoinGame(roomName, playerNameField.getText());
        });

        JButton backButton = createButton("Назад", 180, 570, e -> showJoinMenu());

        addComponentsToPanel(playerNameLabel, playerNameField, doneButton, backButton);
    }

    private void handleJoinGame(String roomName, String playerName) {
        if (!playerName.isEmpty()) {
            coordinator.joinGame(roomName, playerName);
        }
    }

    public void showGame() {
        initializeGameFrame();
        setupGameFrameComponents();
        gameFrame.setVisible(true);
    }

    private void initializeGameFrame() {
        gameFrame = new JFrame("Змейка");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setSize(GAME_FRAME_WIDTH, GAME_FRAME_HEIGHT);
        gameFrame.setResizable(false);
        gameFrame.setVisible(false);
        gameFrame.setLocationRelativeTo(null);
        gameFrame.getContentPane().setBackground(BACKGROUND_COLOR);
        setupKeyListener();

        menuFrame.setVisible(false);
        gameFrame.setLayout(null);
        gameFrame.getContentPane().setBackground(BACKGROUND_COLOR);
    }

    private void setupGameFrameComponents() {
        gameField = createGameField();
        gameField.setBounds(5, 10, FIELD_WIDTH, FIELD_HEIGHT);
        gameField.setBackground(BACKGROUND_COLOR);

        JPanel infoPanel = createInfoPanel();
        infoPanel.setBounds(720, 10, INFO_PANEL_WIDTH, INFO_PANEL_HEIGHT);

        gameFrame.add(gameField);
        gameFrame.add(infoPanel);
    }

    private Field createGameField() {
        return new Field(FIELD_WIDTH, FIELD_HEIGHT);
    }

    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(BACKGROUND_COLOR);

        JLabel infoLabel = createInfoLabel();
        infoPanel.add(infoLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        playersPanel = createPlayersPanel();
        JScrollPane playersScrollPane = createPlayersScrollPane();
        infoPanel.add(playersScrollPane);

        JButton toMenuButton = createToMenuButton();
        infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        infoPanel.add(toMenuButton);

        return infoPanel;
    }

    private JLabel createInfoLabel() {
        JLabel label = new JLabel("  Игроки:");
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private JPanel createPlayersPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BACKGROUND_COLOR);
        updatePlayersPanel(coordinator.getGameState());
        return panel;
    }

    private JScrollPane createPlayersScrollPane() {
        JScrollPane scrollPane = new JScrollPane(playersPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setPreferredSize(new Dimension(INFO_PANEL_WIDTH, 550));
        return scrollPane;
    }

    private JButton createToMenuButton() {
        JButton button = createButton("В меню", STANDARD_BUTTON_SIZE, e -> {
            coordinator.exitToMenu();
            gameFrame.setVisible(false);
            menuFrame.setVisible(true);
        });
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private void updatePlayersPanel(GameState state) {
        if (playersPanel == null) return;

        playersPanel.removeAll();

        for (GamePlayer player : state.getPlayersList()) {
            JPanel playerPanel = createPlayerPanel(player);
            playersPanel.add(playerPanel);
            playersPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        }

        playersPanel.revalidate();
        playersPanel.repaint();
    }

    private JPanel createPlayerPanel(GamePlayer player) {
        JPanel playerPanel = new JPanel();
        playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.X_AXIS));
        playerPanel.setBackground(BACKGROUND_COLOR);
        playerPanel.setMaximumSize(new Dimension(INFO_PANEL_WIDTH, 40));

        JLabel nameLabel = createPlayerNameLabel(player.getName());
        Component horizontalGlue = Box.createHorizontalGlue();
        JLabel scoreLabel = createPlayerScoreLabel(player.getScore());

        playerPanel.add(nameLabel);
        playerPanel.add(horizontalGlue);
        playerPanel.add(scoreLabel);

        return playerPanel;
    }

    private JLabel createPlayerNameLabel(String name) {
        JLabel label = new JLabel(name);
        label.setFont(PLAYER_NAME_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createPlayerScoreLabel(int score) {
        JLabel label = new JLabel(String.valueOf(score));
        label.setFont(PLAYER_SCORE_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    // Вспомогательные методы для создания компонентов UI

    private JLabel createTitleLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(MAIN_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }

    private JLabel createInputLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(TITLE_FONT);
        label.setForeground(TEXT_COLOR);
        label.setBounds(x, y, LABEL_WIDTH, LABEL_HEIGHT);
        return label;
    }

    private JTextField createInputField(int x, int y) {
        JTextField field = new JTextField(1);
        field.setBounds(x, y, INPUT_FIELD_WIDTH, INPUT_FIELD_HEIGHT);
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBackground(BUTTON_COLOR);
        field.setFont(INPUT_FONT);
        field.setForeground(TEXT_COLOR);
        return field;
    }

    private JButton createButton(String text, java.awt.event.ActionListener listener) {
        JButton button = new JButton(text);
        setButtonStyle(button);
        button.addActionListener(listener);
        return button;
    }

    private JButton createButton(String text, Dimension size, java.awt.event.ActionListener listener) {
        JButton button = createButton(text, listener);
        button.setPreferredSize(size);
        button.setMaximumSize(size);
        button.setMinimumSize(size);
        return button;
    }

    private JButton createButton(String text, int x, int y, java.awt.event.ActionListener listener) {
        JButton button = createButton(text, listener);
        button.setBounds(x, y, INPUT_FIELD_WIDTH, INPUT_FIELD_HEIGHT);
        return button;
    }

    private void addComponentsToPanel(JComponent... components) {
        for (JComponent component : components) {
            mainMenuPanel.add(component);
        }
        mainMenuPanel.updateUI();
    }

    private void setButtonStyle(JButton button) {
        button.setFont(BUTTON_FONT);
        button.setBackground(BUTTON_COLOR);
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
    }

    private class Field extends JPanel {
        private GameState state;
        private int cellSize;
        private ConfigData configData;
        private int fieldWidth;
        private int fieldHeight;

        public Field(int fieldWidth, int fieldHeight) {
            setBackground(BACKGROUND_COLOR);
            this.state = coordinator.getGameState();
            this.fieldWidth = fieldWidth;
            this.fieldHeight = fieldHeight;
            this.configData = coordinator.getConfigData();
            this.cellSize = calculateCellSize();
        }

        private int calculateCellSize() {
            return Math.min(this.fieldHeight, this.fieldWidth) /
                    Math.max(configData.height(), configData.width());
        }

        public void redraw(GameState newState) {
            this.state = newState;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2d = (Graphics2D) g;
            enableAntialiasing(g2d);

            drawSnakes(g2d);
            drawGrid(g2d);
            drawFood(g2d);
        }

        private void enableAntialiasing(Graphics2D g2d) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        }

        private void drawSnakes(Graphics2D g2d) {
            for (Snake snake : state.getSnakeList()) {
                ArrayList<Coord> coords = snake.getCoords();
                drawSnake(g2d, coords);
            }
        }

        private void drawSnake(Graphics2D g2d, ArrayList<Coord> coords) {
            int prevX = 0, prevY = 0;
            for (int i = 0; i < coords.size(); ++i) {
                Coord coord = coords.get(i);
                if (i == 0) {
                    drawSnakeHead(g2d, coord);
                    prevX = coord.x();
                    prevY = coord.y();
                } else {
                    if (i == 1) {
                        g2d.setColor(SNAKE_BODY_COLOR);
                    }
                    Coord newCoord = calculateNewCoord(coord, prevX, prevY);
                    drawSnakeSegment(g2d, newCoord);
                    prevX = newCoord.x();
                    prevY = newCoord.y();
                }
            }
        }

        private void drawSnakeHead(Graphics2D g2d, Coord coord) {
            g2d.setColor(SNAKE_HEAD_COLOR);
            drawSnakeSegment(g2d, coord);
        }

        private void drawSnakeSegment(Graphics2D g2d, Coord coord) {
            g2d.fillOval(coord.x() * cellSize + 1, coord.y() * cellSize + 1,
                    cellSize - 2, cellSize - 2);
        }

        private Coord calculateNewCoord(Coord coord, int prevX, int prevY) {
            int newX = calculateNewX(coord.x() + prevX);
            int newY = calculateNewY(coord.y() + prevY);
            return new Coord(newX, newY);
        }

        private int calculateNewX(int x) {
            if (x >= configData.width()) {
                return 0;
            } else if (x < 0) {
                return configData.width() - 1;
            } else {
                return x;
            }
        }

        private int calculateNewY(int y) {
            if (y >= configData.height()) {
                return 0;
            } else if (y < 0) {
                return configData.height() - 1;
            } else {
                return y;
            }
        }

        private void drawGrid(Graphics2D g2d) {
            g2d.setColor(GRID_COLOR);
            drawVerticalGridLines(g2d);
            drawHorizontalGridLines(g2d);
        }

        private void drawVerticalGridLines(Graphics2D g2d) {
            for (int i = 0; i < configData.width() + 1; ++i) {
                int x = i * cellSize;
                g2d.drawLine(x, 0, x, configData.height() * cellSize);
            }
        }

        private void drawHorizontalGridLines(Graphics2D g2d) {
            for (int i = 0; i < configData.height() + 1; ++i) {
                int y = i * cellSize;
                g2d.drawLine(0, y, configData.width() * cellSize, y);
            }
        }

        private void drawFood(Graphics2D g2d) {
            g2d.setColor(FOOD_COLOR);
            for (Coord coord : state.getFoodList()) {
                drawFoodItem(g2d, coord);
            }
        }

        private void drawFoodItem(Graphics2D g2d, Coord coord) {
            g2d.fillRoundRect(coord.x() * cellSize + 1, coord.y() * cellSize + 1,
                    cellSize - 2, cellSize - 2, cellSize / 2, cellSize / 2);
        }
    }
}