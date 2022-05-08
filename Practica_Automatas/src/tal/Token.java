package tal;

/**
 * Elementos básicos del lenguaje.
 */
public class Token
{
/** Tipos posibles de los tokens. */
public static enum Type
{
    /** Palabra reservada {@code entero}. *//** Palabra reservada {@code entero}. */
    ENTERO,
    /** Palabra reservada {@code cadena}. */
    CADENA,
    /** Palabra reservada {@code si}. */
    SI,
    /** Palabra reservada {@code sino}. */
    SINO,
    /** Palabra reservada {@code mientras}. */
    MIENTRAS,
    /** Palabra reservada {@code fin}. */
    FIN,
    /** Palabra reservada {@code imprimir}. */
    IMPRIMIR,
    /** Identificadores (nombres de variables). */
    ID,
    /** Número entero. */
    INTVAL,
    /** Cadena de caracteres entre dobles comillas. */
    STRVAL,
    /** Operador de asignación: {@code =} */
    ASIGN,
    /** Operadores de suma y resta: {@code +}, {@code -} */
    SUM,
    /** Operadores de multiplicación y división: {@code *}, {@code /} */
    MUL,
    /** Operadores relacionales:
     *  {@code ==}, {@code !=},
     *  {@code <}, {@code <=},
     *  {@code >}, {@code >=} */
    REL,
    /** Operador de negación: {@code !} */
    NEG,
    /** Operador de disyunción: {@code ||} */
    OR,
    /** Operador de conjunción: {@code &&} */
    AND,
    /** Paréntesis izquierdo: {@code (} */
    IPAR,
    /** Paréntesis derecho: {@code )} */
    DPAR,
    /** Fin de fichero. */
    EOF
}

/** Tipo del token. */
public final Type type;
/** Nombre del token. */
public final String name;
/** Fila donde está el token en el fichero analizado. */
public final int row;
/** Columna donde está el token en el fichero analizado. */
public final int column;

/**
 * Construye un token.
 * @param type
 * @param name
 * @param row
 * @param column
 */
public Token(Type type, String name, int row, int column)
{
    this.type   = type;
    this.name   = name;
    this.row    = row;
    this.column = column;
}

//------------------------------------------------------------------------
@Override public String toString()
{
    StringBuilder sb = new StringBuilder();
    sb.append("Token ");
    sb.append(type);
    sb.append(": ");
    sb.append(name);
    return sb.toString();
}

} // Token
