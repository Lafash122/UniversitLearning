## Синтаксический анализатор. Методы трансляции и компиляции
В данной директории представлен синтаксический анализатор оператора цикла for языка C/C++. Помимо своего анализатора в программу подключен генератор анализаторов по описанной грамматике (antlr4).
## Сборка
Для сборки файлов antlr4: `java -jar libs\antlr-4.13.2-complete.jar -visitor -listener application\antlr\ForGrammar.g4`.
Для компиляции программы: `javac -d out -cp .;libs/antlr-4.13.2-complete.jar application/Main.java`
Для запуска программы: `java -cp out;libs/antlr-4.13.2-complete.jar application.Main`
