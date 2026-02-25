grammar LabeledExpr;

/** Regla de inicio: un programa es una secuencia de una o más sentencias */
prog:   stat+ ;

/** Una sentencia puede ser: expresión a imprimir, asignación o línea vacía */
stat:   expr NEWLINE        # printExpr   // expresión seguida de salto de línea → imprimir resultado
    |   ID '=' expr NEWLINE # assign      // asignación: variable = expresión
    |   NEWLINE             # blank       // línea vacía: ignorar
    ;

/**
 * Expresión aritmética con precedencia manejada automáticamente por ANTLR 4.
 * Las alternativas más arriba tienen mayor precedencia (* y / antes que + y -).
 * La recursividad por la izquierda es resuelta internamente por ANTLR.
 */
expr:   expr op=('*'|'/') expr  # MulDiv  // multiplicación o división (mayor precedencia)
    |   expr op=('+'|'-') expr  # AddSub  // suma o resta (menor precedencia)
    |   INT                     # int     // literal entero
    |   ID                      # id      // referencia a variable
    |   '(' expr ')'            # parens  // agrupación con paréntesis
    ;

/** Nombres de token para referenciarlos como constantes Java en el visitor */
MUL :   '*' ;   // token de multiplicación
DIV :   '/' ;   // token de división
ADD :   '+' ;   // token de suma
SUB :   '-' ;   // token de resta

ID      :   [a-zA-Z]+ ;     // identificadores: sólo letras
INT     :   [0-9]+ ;         // enteros sin signo
NEWLINE :   '\r'? '\n' ;     // salto de línea (fin de sentencia)
WS      :   [ \t]+ -> skip ; // espacios y tabulaciones: ignorar completamente
