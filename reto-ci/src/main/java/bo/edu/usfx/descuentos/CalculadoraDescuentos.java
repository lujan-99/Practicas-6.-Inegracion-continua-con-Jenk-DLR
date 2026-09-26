package bo.edu.usfx.descuentos;

/**
 * Calcula el precio final de un producto despues de aplicar un descuento.
 *
 * Reglas:
 *  - porcentajeDescuento fuera de [0, 100]  -> IllegalArgumentException
 *  - precioOriginal <= 0                    -> IllegalArgumentException
 *  - el resultado se redondea a dos decimales (medio hacia arriba)
 *  - descuento 0   -> el precio no cambia
 *  - descuento 100 -> el precio final es 0
 */
public class CalculadoraDescuentos {

    public double calcularPrecioFinal(double precioOriginal, double porcentajeDescuento) {
        if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100");
        }
        if (precioOriginal <= 0) {
            throw new IllegalArgumentException("El precio original debe ser mayor que cero");
        }
        double precioFinal = precioOriginal * (1 - porcentajeDescuento / 100);
        return redondear(precioFinal);
    }

    /**
     * Aplica un descuento por cantidad sobre el precio unitario.
     *
     * Reglas:
     *  - cantidad &lt;= 0                -> IllegalArgumentException
     *  - 1 o 2 unidades                 -> sin descuento
     *  - 3 o 4 unidades                 -> 5 %
     *  - 5 a 9 unidades                 -> 10 %
     *  - 10 unidades o mas              -> 15 %
     *  - el total se redondea a dos decimales (medio hacia arriba)
     */
    public double calcularPrecioPorCantidad(double precioUnitario, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        double porcentajePorCantidad;
        if (cantidad >= 10) {
            porcentajePorCantidad = 15;
        } else if (cantidad >= 5) {
            porcentajePorCantidad = 10;
        } else if (cantidad >= 3) {
            porcentajePorCantidad = 5;
        } else {
            porcentajePorCantidad = 0;
        }
        double total = precioUnitario * cantidad;
        return redondear(total * (1 - porcentajePorCantidad / 100));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
