# Calculadora ANTLR 4 — Capítulo 4

Implementación en Java de la calculadora de expresiones aritméticas presentada en el Capítulo 4 de *The Definitive ANTLR 4 Reference* (Parr, 2013).

---

## Características

- Operaciones: suma `+`, resta `-`, multiplicación `*`, división `/`
- Agrupación con paréntesis de cualquier profundidad
- Variables: asignación (`a = 5`) y lectura posterior
- Precedencia correcta sin reglas auxiliares (gracias a las reglas *left-recursive* de ANTLR 4)
- Recuperación automática de errores sintácticos

---

## Estructura

```
antlr-calculator/
├── antlr-4.11.1-complete.jar      ← Herramienta ANTLR + runtime Java
├── README.md                      ← Este archivo
└── src/
    ├── LabeledExpr.g4             ← Gramática (fuente principal)
    ├── EvalVisitor.java           ← Visitor que evalúa el árbol
    ├── Calc.java                  ← Programa principal (main)
    ├── t.expr                     ← Archivo de prueba
    │
    │   ── Generados automáticamente por ANTLR ──
    ├── LabeledExprLexer.java
    ├── LabeledExprParser.java
    ├── LabeledExprVisitor.java
    └── LabeledExprBaseVisitor.java
```

---

## Prerrequisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java JDK | 11 | Se necesita `javac` para compilar |
| ANTLR | 4.x | JAR incluido en el repo |

---

## Compilación y ejecución

### 1 — Generar el lexer, parser y visitor desde la gramática

```bash
cd src/
java -jar ../antlr-4.11.1-complete.jar -visitor -no-listener LabeledExpr.g4
```

Esto crea los cuatro archivos Java generados que aparecen en la estructura anterior.

### 2 — Compilar todo el código Java

```bash
javac -cp ../antlr-4.11.1-complete.jar *.java
```

### 3 — Ejecutar la calculadora

```bash
# Sobre un archivo
java -cp .:../antlr-4.11.1-complete.jar Calc t.expr

# Modo interactivo (stdin)
java -cp .:../antlr-4.11.1-complete.jar Calc
```

> En Windows reemplaza `:` por `;` en el classpath.

---

## Archivo de prueba `t.expr`

```
193
a = 5
b = 6
a+b*2
(1+2)*3
```

### Salida esperada

```
193
17
9
```

**¿Por qué `17`?** Porque `*` tiene mayor precedencia que `+`, así que `a+b*2` se evalúa como `5 + (6*2) = 5 + 12 = 17`.  
**¿Por qué `9`?** Los paréntesis fuerzan `(1+2)*3 = 3*3 = 9`.

---

## Cómo funciona: pipeline ANTLR

```
Archivo t.expr
     │
     ▼
CharStream          ← flujo de caracteres
     │
     ▼
LabeledExprLexer    ← tokeniza: INT, ID, '+', '-', '*', '/', '(', ')', NEWLINE
     │
     ▼
CommonTokenStream   ← buffer de tokens
     │
     ▼
LabeledExprParser   ← construye el árbol de análisis aplicando las reglas de la gramática
     │
     ▼
ParseTree           ← árbol resultado
     │
     ▼
EvalVisitor         ← recorre el árbol y calcula los valores
     │
     ▼
Salida por pantalla
```

---

## Gramática: `LabeledExpr.g4`

El punto clave es el uso de **etiquetas de alternativa** (`# nombreEtiqueta`). Sin ellas, ANTLR genera un único método `visitStat()` para todas las sentencias. Con las etiquetas, genera un método específico por caso:

| Etiqueta | Método generado | Cuándo se dispara |
|---|---|---|
| `# printExpr` | `visitPrintExpr()` | Línea con sólo una expresión |
| `# assign` | `visitAssign()` | Asignación `ID = expr` |
| `# blank` | `visitBlank()` | Línea vacía |
| `# MulDiv` | `visitMulDiv()` | Multiplicación o división |
| `# AddSub` | `visitAddSub()` | Suma o resta |
| `# int` | `visitInt()` | Literal entero |
| `# id` | `visitId()` | Referencia a variable |
| `# parens` | `visitParens()` | Expresión entre paréntesis |

### Precedencia de operadores

ANTLR 4 resuelve la precedencia por el **orden de las alternativas** en una regla recursiva por la izquierda. Las que aparecen primero ligan más:

```
expr:   expr ('*'|'/') expr   ← mayor precedencia (va primero)
    |   expr ('+'|'-') expr   ← menor precedencia (va después)
    |   ...
```

Esto elimina la necesidad de las reglas auxiliares `term` y `factor` de los parsers LL clásicos.

---

## Visitor: `EvalVisitor.java`

La clase extiende `LabeledExprBaseVisitor<Integer>` — el tipo genérico `Integer` indica que todos los métodos devuelven un entero.

### Memoria de variables

```java
Map<String, Integer> memory = new HashMap<>();

// Guardar: visitAssign()
memory.put(id, value);

// Leer: visitId()
if (memory.containsKey(id)) return memory.get(id);
return 0; // variable no definida → 0
```

### Patrón de evaluación recursiva

```java
// Ejemplo: visitMulDiv
int left  = visit(ctx.expr(0)); // evalúa subárbol izquierdo
int right = visit(ctx.expr(1)); // evalúa subárbol derecho
if (ctx.op.getType() == LabeledExprParser.MUL) return left * right;
return left / right;
```

Cada método delega en `visit()` hacia sus hijos, propagando los valores de abajo arriba por el árbol.

---

## Visitor vs Listener

ANTLR 4 ofrece dos mecanismos de recorrido. Se eligió **Visitor** porque la calculadora necesita devolver valores desde cada nodo:

| Criterio | Visitor (este proyecto) | Listener |
|---|---|---|
| Control del recorrido | El desarrollador llama `visit()` explícitamente | `ParseTreeWalker` recorre automáticamente |
| Valor de retorno | Sí — cada método devuelve `T` | No — los métodos son `void` |
| Riesgo | Olvidar `visit()` en un hijo omite ese subárbol | Ninguno: el walker garantiza recorrer todo |
| Uso típico | Evaluadores, intérpretes, compiladores | Traductores, extractores de código |

