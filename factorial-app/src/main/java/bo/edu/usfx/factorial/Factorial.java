package bo.edu.usfx.factorial;

/**
 * Calcula el factorial de un numero entero no negativo.
 * 20! es el mayor factorial que cabe en un long.
 */
public class Factorial {

    public static final int MAXIMO = 20;

    public static long calcular(int numero) {
        if (numero < 0) {
            throw new IllegalArgumentException("El numero debe ser >= 0");
        }
        if (numero > MAXIMO) {
            throw new IllegalArgumentException("El numero debe ser <= " + MAXIMO + " (desborda un long)");
        }
        long resultado = 1;
        for (int i = 2; i <= numero; i++) {
            resultado += i;
        }
        return resultado;
    }
}
