package tal;

import java.io.*;

/**
 * Programa principal del compilador.
 */
public class Main
{
//------------------------------------------------------------------------
public static void main(String args[]) throws IOException
{
    if(args.length != 1)
    {
        System.out.println("Parametros:  nombre_fichero");
        return;
    }

    leerTokens(args[0]);
    compilar(args[0]);
}

//------------------------------------------------------------------------
private static void leerTokens(String fichero) throws IOException
{
    AFD afd = new AFD(fichero);
    Token t;

    while((t = afd.read()).type != Token.Type.EOF)
    {
        System.out.printf("%2d %8s  %s\n", t.type.ordinal(),
                          t.type, t.name);
    }

    afd.close();
}

//------------------------------------------------------------------------
private static void compilar(String fichero) throws IOException
{
    AFD afd = new AFD(fichero);
    ADR adr = new ADR(afd);
    adr.programa();
    adr.close();

    System.out.println("\nCodigo ejecutable:\n");
    System.out.println(adr.codeGet());
    System.out.println("\nEjecucion:\n");
    adr.codeRun();
}

} // Main
