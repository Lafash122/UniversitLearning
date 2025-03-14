# Second Lab - Stack Calculator
## Files `*.java`
- `Main.java` creates the object of Handler class to handle input;
- `Handler.java` handles input data and that works with input file or console;
- `Context.java` is a context for commands;
- `CommandFactory.java` is a fabric-class that creates the successor classes of the Command, it takes the path to them from `out/FabricConfig.cfg`;
- `Command.java` is an abstract class describing a command;
- `CommandAdd.java`, `CommandComment.java`, `CommandPop.java` and etc; classes that implement specific commands
- `DivisionByZero.java`, `InvalidParameter.java` and etc; classes for exception handling

## Compiling and running
### To compile use next command
```cmd
javac -d out commands/*.java
javac -d out -sourcepath . main/Main.java
```
### To run file-mode use
```cmd
java -cp out -Djava.util.logging.config.file=logging.properties main.Main <input.txt>
```
### To run console-mode use
```cmd
java -cp out -Djava.util.logging.config.file=logging.properties main.Main
```

## Information about the input files
- File `example.txt` includes **usage example**
- File `in.txt` includes **all commands**

- File `logging.properties` includes **logger configuration**
- File `out/FabricConfig.cfg` includes **fabric configuration**
