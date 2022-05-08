package tal;

import java.io.*;

/**
 * Clase base para implementar analizadores léxicos
 * mediante autómatas finitos deterministas.
 */
public class ALex
{
private final Reader m_is;
private char m_char;
private boolean m_charReaded, m_reading;
private int m_row = 1, m_column;
private final StringBuilder m_name = new StringBuilder();
private Token m_token;
private Runnable m_state, m_start;

/**
 * Construye un analizador léxico.
 * @param fichero Fichero de texto que se debe analizar.
 * @throws IOException
 */
public ALex(String fichero) throws IOException
{
    m_is = new BufferedReader(new FileReader(fichero));
}

/**
 * Función para indicar cuál es el estado inicial del autómata.
 * @param inicio Estado inicial del autómata.
 */
public final void setStart(Runnable inicio)
{
    if(m_start != null)
        throw new AssertionError("Ya se indicó el estado inicial.");

    m_start = inicio;
}

/**
 * Reinicia el autómata descartando los caracteres leídos.
 * Esta función se debe usar para fiunalizar la lectura de los
 * comentarios.
 */
public final void restart()
{
    state(m_start);
    m_name.setLength(0);
}

/**
 * Cierra el fichero de texto que se ha analizado.
 * @throws IOException
 */
public final void close() throws IOException
{
    m_is.close();
}

/**
 * Lee el siguiente token del fichero de texto.
 * @return Token leído.
 * @throws IOException
 */
public final Token read() throws IOException
{
    m_reading = true;
    restart();

    while(m_reading)
    {
        readChar();
        m_state.run();
    }

    return m_token;
}

private void readChar() throws IOException
{
    if(m_charReaded)
    {
        m_charReaded = false;
    }
    else
    {
        m_char = (char)m_is.read();
        m_column++;

        if(m_char == '\n')
        {
            m_row++;
            m_column = 0;
        }
    }
}

/**
 * Interrumpe la compilación con una excepción en caso de que se lea un
 * carácter no permitido.
 */
public final void error()
{
    throw new RuntimeException("Caracter no permitido en "+
                               m_row +":"+ m_column +" : "+ m_char);
}

/**
 * Cuando un estado del autómata termine de leer un token debe llamar
 * a esta función. Los estados que llamen a esta función serán finales.
 * @param tipo Tipo del token leído.
 */
public final void token(Token.Type tipo)
{
    m_token = new Token(tipo, m_name.toString(),
                        m_row, m_column - m_name.length());
    m_reading = false;
    m_charReaded = true;
}

/**
 * Esta función permite dibujar una flecha desde el estado que
 * llama a esta función al estado indicado como parámetro.
 * @param estado Estado actual tras leer el siguiente carácter.
 */
public final void state(Runnable estado)
{
    m_name.append(m_char);
    m_state = estado;
}

/**
 * Esta función hace lo mismo que <code>estado</code>
 * pero no añade el carácter leído al nombre del token.
 * <p>Se puede utilizar para descartar las dobles comillas de las
 * cadenas de caracteres y los comentarios.
 * @param estado Estado actual tras leer el siguiente carácter.
 * @see #state(Runnable)
 */
public final void stateNoChar(Runnable estado)
{
    if(estado == m_start)
        m_name.setLength(0);

    m_state = estado;
}

/**
 * Indica si el siguiente carácter coincide con el indicado.
 * @param c Caracter a comparar.
 * @return true si el siguiente carácter coincide con c.
 */
public final boolean isChar(char c)
{
    return m_char == c;
}

/**
 * Indica si el siguiente carácter puede ser el primero
 * del nombre de un identificador.
 * @return true si el carácter es válido.
 */
public final boolean isIdCharStart()
{
    return Character.isLetter(m_char) ||
           m_char == '_';
}

/**
 * Indica si el siguiente carácter es válido para un identificador,
 * @return true si el carácter es válido.
 */
public final boolean isIdChar()
{
    return Character.isLetter(m_char) ||
           Character.isDigit(m_char)  ||
           m_char == '_';
}

/**
 * Indica si el siguiente carácter es un dígito.
 * @return true si el carácter es un dígito.
 */
public final boolean isDigitChar()
{
    return Character.isDigit(m_char);
}

/**
 * Indica si el siguiente carácter es un espacio, tabulación,
 * salto de línea...
 * @return true si el carácter es un espacio en blanco.
 */
public final boolean isSpaceChar()
{
    return Character.isWhitespace(m_char);
}

/**
 * Indica si se ha llegado al final del fichero.
 * @return true si se ha llegado al final del fichero.
 */
public final boolean isEofChar()
{
    return m_char == Character.MAX_VALUE;
}

} // ALex
