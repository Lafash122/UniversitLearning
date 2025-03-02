# Firs Lab - Words Counter
## Files `*.java`
- `Main.java` - create classes to handle input and output files;
- `Collector.java` - read input file and collect info: number of words and map (format: word, word number)
- `Writer.java` - sort map by number of words in descending order and write this information in file (`.csv` format: word;frequency;percents)
### `Collector.java`
Checks if the input file exists. And then it reads a string from the files and uses regular expressions to search for words in it. As a result, it gets TreeMap: word=wordNumbers and total number of words
### `Writer.java`
Creates a new file if it doesn't exist. Then splits it into a Set, converts it into a Stream object, sorts it by word frequency in descending order and writes the result output file.
## Compiling and running
To compile use next command
```cmd
javac *.java
```

To run use
```cmd
java Main in.txt out.csv
```

## Information about the input files
- File `tmp.txt` includes **"Crusoe"**
- File `in.txt` includes **some suspicious character combinations**
