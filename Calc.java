import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Calc: programa principal que conecta todas las piezas del pipeline ANTLR.
 *
 * Flujo de ejecución:
 *   1. Abrir el fichero de entrada (o leer de stdin si no se proporciona ruta).
 *   2. Crear el flujo de caracteres (CharStream).
 *   3. Instanciar el lexer y tokenizar la entrada.
 *   4. Crear el buffer de tokens (CommonTokenStream).
 *   5. Instanciar el parser y construir el árbol de análisis.
 *   6. Recorrer el árbol con EvalVisitor para evaluar las expresiones.
 *
 * Uso:
 *   java Calc [archivo_entrada]
 *
 * Si no se indica archivo, lee de la entrada estándar (terminar con Ctrl+D).
 */
public class Calc {
    public static void main(String[] args) throws Exception {

        // ── Paso 1: determinar la fuente de entrada ──────────────────────────
        String inputFile = null;
        if (args.length > 0) inputFile = args[0];

        InputStream is = System.in;                          // por defecto: stdin
        if (inputFile != null) is = new FileInputStream(inputFile);

        // ── Paso 2: crear el flujo de caracteres que alimenta al lexer ───────
        CharStream input = CharStreams.fromStream(is);

        // ── Paso 3: instanciar el lexer generado por ANTLR ───────────────────
        LabeledExprLexer lexer = new LabeledExprLexer(input);

        // ── Paso 4: envolver los tokens en un buffer para el parser ──────────
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // ── Paso 5: instanciar el parser y arrancar desde la regla 'prog' ────
        LabeledExprParser parser = new LabeledExprParser(tokens);
        ParseTree tree = parser.prog(); // devuelve la raíz del árbol de análisis

        // ── Paso 6: recorrer el árbol con el visitor de evaluación ───────────
        EvalVisitor eval = new EvalVisitor();
        eval.visit(tree); // los resultados se imprimen dentro del visitor
    }
}
