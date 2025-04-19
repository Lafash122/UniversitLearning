# Third Lab - A Game

## Files `*.java`
- `Main.java` creates the object of Game class to play a game;
- `ScoreTableHandler.java` handles score table from `scores` directory;
- `Game.java` contains the main implementation of the game;
- `Deck.java` a class that implements a deck of cards;
- `Kard.java` a class that implements a playing card;
- `Player.java`, `Dealer.java` and etc; classes that implement player and dealer behaviour;
- `Model.java`, `ConsoleViewer.java`, `ConsoleController` and etc; classes implementing the MVC model;

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
