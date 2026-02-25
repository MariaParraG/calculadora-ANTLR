import java.util.HashMap;
import java.util.Map;

/**
 * EvalVisitor: recorre el árbol de análisis generado por LabeledExprParser
 * y calcula el valor entero de cada expresión.
 *
 * Extiende LabeledExprBaseVisitor<Integer> para que cada método visit
 * devuelva un Integer como resultado de la evaluación.
 */
public class EvalVisitor extends LabeledExprBaseVisitor<Integer> {

    /** Memoria de la calculadora: almacena pares nombre_variable → valor */
    Map<String, Integer> memory = new HashMap<String, Integer>();

    // -----------------------------------------------------------------------
    // Reglas de sentencia (stat)
    // -----------------------------------------------------------------------

    /**
     * Sentencia de asignación: ID '=' expr NEWLINE
     * Evalúa la expresión del lado derecho y guarda el resultado en memoria.
     */
    @Override
    public Integer visitAssign(LabeledExprParser.AssignContext ctx) {
        String id    = ctx.ID().getText();  // nombre de la variable (lado izquierdo)
        int    value = visit(ctx.expr());   // evalúa la expresión del lado derecho
        memory.put(id, value);              // almacena el valor en la memoria
        return value;
    }

    /**
     * Sentencia de impresión: expr NEWLINE
     * Evalúa la expresión e imprime el resultado por pantalla.
     */
    @Override
    public Integer visitPrintExpr(LabeledExprParser.PrintExprContext ctx) {
        Integer value = visit(ctx.expr()); // evalúa el hijo de tipo expr
        System.out.println(value);         // muestra el resultado
        return 0;                          // valor de retorno sin uso real
    }

    /**
     * Línea vacía: NEWLINE
     * No hace nada; sólo devuelve 0.
     */
    @Override
    public Integer visitBlank(LabeledExprParser.BlankContext ctx) {
        return 0;
    }

    // -----------------------------------------------------------------------
    // Reglas de expresión (expr)
    // -----------------------------------------------------------------------

    /**
     * Literal entero: INT
     * Convierte el texto del token a un Integer de Java.
     */
    @Override
    public Integer visitInt(LabeledExprParser.IntContext ctx) {
        return Integer.valueOf(ctx.INT().getText());
    }

    /**
     * Referencia a variable: ID
     * Busca el identificador en la memoria; si no existe devuelve 0.
     */
    @Override
    public Integer visitId(LabeledExprParser.IdContext ctx) {
        String id = ctx.ID().getText();
        if (memory.containsKey(id)) return memory.get(id);
        return 0; // variable no definida → se trata como cero
    }

    /**
     * Multiplicación o división: expr op=('*'|'/') expr
     * Evalúa ambos operandos recursivamente y aplica el operador correcto.
     */
    @Override
    public Integer visitMulDiv(LabeledExprParser.MulDivContext ctx) {
        int left  = visit(ctx.expr(0)); // operando izquierdo
        int right = visit(ctx.expr(1)); // operando derecho
        if (ctx.op.getType() == LabeledExprParser.MUL) return left * right;
        // Es división: comprobar divisor cero
        if (right == 0) {
            System.err.println("Error: división por cero");
            return 0;
        }
        return left / right;
    }

    /**
     * Suma o resta: expr op=('+'|'-') expr
     * Evalúa ambos operandos recursivamente y aplica el operador correcto.
     */
    @Override
    public Integer visitAddSub(LabeledExprParser.AddSubContext ctx) {
        int left  = visit(ctx.expr(0)); // operando izquierdo
        int right = visit(ctx.expr(1)); // operando derecho
        if (ctx.op.getType() == LabeledExprParser.ADD) return left + right;
        return left - right;
    }

    /**
     * Expresión entre paréntesis: '(' expr ')'
     * Simplemente delega la evaluación al hijo interior.
     */
    @Override
    public Integer visitParens(LabeledExprParser.ParensContext ctx) {
        return visit(ctx.expr()); // evalúa la subexpresión entre paréntesis
    }
}
