package bo.edu.usfx.descuentos;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CalculadoraDescuentosTest {

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

    // Pruebas del Ejercicio 2: las que faltaban para que la puerta de
    // cobertura del 80% deje de saltar tras agregar calcularPrecioPorCantidad.

    @ParameterizedTest(name = "{0} uds a {1} = {2}")
    @CsvSource({
            "1,  10.00,  10.00",   // tramo 0 %
            "2,  10.00,  20.00",   // tramo 0 %
            "3,  10.00,  28.50",   // tramo 5 %
            "4,  10.00,  38.00",   // tramo 5 %
            "5,  10.00,  45.00",   // tramo 10 %
            "9,  10.00,  81.00",   // tramo 10 %
            "10, 10.00,  85.00",   // tramo 15 %
            "15, 10.00, 127.50",   // tramo 15 %
            "3,   3.33,   9.49"    // redondeo a dos decimales
    })
    void descuentoPorCantidadAplicaElTramoCorrespondiente(int cantidad, double unitario, double esperado) {
        assertEquals(esperado, calculadora.calcularPrecioPorCantidad(unitario, cantidad), 0.001);
    }

    @ParameterizedTest(name = "cantidad {0} es invalida")
    @ValueSource(ints = {0, -1, -50})
    void cantidadNoPositivaLanzaExcepcion(int cantidad) {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularPrecioPorCantidad(10.00, cantidad));
        assertEquals("La cantidad debe ser mayor que cero", e.getMessage());
    }

    // TDD del Ejercicio 3: la prueba se escribe ANTES que la funcionalidad.
    // Este commit es el rojo deliberado: el metodo todavia no tiene el tramo
    // de 20 unidades, asi que la asercion falla.
    @ParameterizedTest(name = "pedido grande: {0} uds a {1} = {2}")
    @CsvSource({
            "20, 10.00, 160.00",   // tramo 20 %: 200 * 0.80
            "25, 10.00, 200.00",   // tramo 20 %: 250 * 0.80
            "50, 10.00, 400.00"    // tramo 20 %: 500 * 0.80
    })
    void descuentoPorVolumenEscalaA20PorCiento(int cantidad, double unitario, double esperado) {
        assertEquals(esperado, calculadora.calcularPrecioPorCantidad(unitario, cantidad), 0.001);
    }
}
