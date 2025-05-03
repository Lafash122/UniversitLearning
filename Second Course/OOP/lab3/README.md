# Third Lab - A Game

## Files `*.java`
- `Main.java` creates the object of Game class to play a game;
- `ScoreTableHandler.java` handles score table from `scores` directory;
- `Game.java` contains the main implementation of the game;
- `Deck.java` a class that implements a deck of cards;
- `Kard.java` a class that implements a playing card;
- `Player.java` and `Dealer.java`; classes that implement player and dealer behaviour;
- `Model.java`, `ConsoleViewer.java`, `ConsoleController`, `GraphicView` and `GraphicController`; classes implementing the MVC model;
- `GraphicView` and `GraphicController`; classes implementing the grapic mode of game;
- `ConsoleViewer.java` and `ConsoleController`; classes implementing the console mode of game;

## Compiling and running program
### To compile use next command
```cmd
javac -d out -sourcepath . game/Main.java
```
### To run
```cmd
java -cp out game.Main
```

## Information about the other files
- Files in `resourses/` directory is **resourses**
