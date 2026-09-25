package bo.edu.usfx.factorial;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class FactorialTest {

    @Test
    void factorialDeCeroEsUno() {
        assertEquals(1, Factorial.calcular(0));
    }

    @Test
    void factorialDeCinco() {
        assertEquals(120, Factorial.calcular(5));
    }

    @ParameterizedTest(name = "{0}! = {1}")
    @CsvSource({"1, 1", "2, 2", "3, 6", "10, 3628800", "20, 2432902008176640000"})
    void factorialesConocidos(int numero, long esperado) {
        assertEquals(esperado, Factorial.calcular(numero));
    }

    @Test
    void numeroNegativoLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> Factorial.calcular(-1));
    }

    @Test
    void numeroQueDesbordaLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> Factorial.calcular(21));
    }
}
