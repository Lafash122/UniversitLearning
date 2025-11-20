grammar ForGrammar;

@header {
package application.antlr;
}

forStmt : 'for' '(' init? ';' cond? ';' loop? ')' '{' body '}' ;

init : NAME '=' NUMBER ;

cond : NAME comOp NUMBER ;

loop : NAME ( '++' | '--' ) ;

body : statementList ;

statementList : statement statementList | /* empty */ ;

statement : NAME '=' NUMBER ';' | forStmt ;

comOp : '==' | '!=' | '<=' | '>=' | '<' | '>' ;

NAME : LETTER (LETTER | DIGIT)* ;
NUMBER : '-'? DIGIT+ ;

fragment LETTER : [a-zA-Z_] ;
fragment DIGIT : [0-9] ;

WS : (' ' |'\n' |'\r' )+ -> skip ;