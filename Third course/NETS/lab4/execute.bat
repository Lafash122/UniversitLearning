@echo off

echo Compiling...
javac -cp .;protobuf-java-3.21.5.jar -d out -sourcepath . app/main/Main.java

echo Running...
java -cp out;protobuf-java-3.21.5.jar app.main.Main

pause