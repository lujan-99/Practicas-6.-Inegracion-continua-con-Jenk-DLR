package bo.edu.usfx.descuentos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraDescuentosPrueba {

    private CalculadoraDescuentos calculadora;

    @BeforeEach
    void preparar() {
        calculadora = new CalculadoraDescuentos();
    }

    @Test
    void sinDescuentoElPrecioNoCambia() {
        assertEquals(150.00, calculadora.calcularPrecioFinal(150.00, 0), 0.001);
    }

    @Test
    void descuentoTotalDejaElPrecioEnCero() {
        assertEquals(0.00, calculadora.calcularPrecioFinal(80.00, 100), 0.001);
    }

    @ParameterizedTest(name = "{0} con {1}% = {2}")
    @CsvSource({
            "100.00, 10, 90.00",
            "200.00, 25, 150.00",
            "59.90, 50, 29.95",
            "33.33, 10, 30.00",
            "19.99, 15, 16.99"
    })
    void aplicaElDescuentoYRedondeaADosDecimales(double precio, double porcentaje, double esperado) {
        assertEquals(esperado, calculadora.calcularPrecioFinal(precio, porcentaje), 0.001);
    }

    @ParameterizedTest(name = "descuento {0}% es invalido")
    @ValueSource(doubles = {-1, -0.01, 100.01, 150})
    void porcentajeFueraDeRangoLanzaExcepcion(double porcentaje) {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularPrecioFinal(100.00, porcentaje));
    }

    @ParameterizedTest(name = "precio {0} es invalido")
    @ValueSource(doubles = {0, -10})
    void precioNoPositivoLanzaExcepcion(double precio) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularPrecioFinal(precio, 10));
        assertEquals("El precio original debe ser mayor que cero", e.getMessage());
    }
}
