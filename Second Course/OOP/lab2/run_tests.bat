@echo off

echo Compiling sorce files
javac -d out commands/*.java
javac -d out -sourcepath . main/Main.java

echo Compiling tests
javac -cp "libs/*;out" -d out tests/commands/*.java
javac -cp "libs/*;out" -d out tests/tools/*.java

echo Running tests
java -jar libs/junit-platform-console-standalone-1.12.1.jar -cp out --scan-class-path

pause