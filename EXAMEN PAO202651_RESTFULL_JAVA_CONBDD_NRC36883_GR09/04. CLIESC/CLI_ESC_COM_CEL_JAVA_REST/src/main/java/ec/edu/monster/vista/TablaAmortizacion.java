package ec.edu.monster.vista;

import ec.edu.monster.modelo.CuotaAmortizacion;
import java.math.BigDecimal;

/** Columnas de la tabla de amortización con el formato del enunciado (incluye la fila 0). */
final class TablaAmortizacion {
    private TablaAmortizacion() { }

    static ModeloTabla<CuotaAmortizacion> modelo() {
        return new ModeloTabla<>(
            ModeloTabla.Columna.numero("# Cuota", CuotaAmortizacion::getNumeroCuota),
            ModeloTabla.Columna.numero("Valor Cuota", c -> celda(c.getValorCuota())),
            ModeloTabla.Columna.numero("Interés Pagado", c -> celda(c.getInteresPagado())),
            ModeloTabla.Columna.numero("Capital Pagado", c -> celda(c.getCapitalPagado())),
            ModeloTabla.Columna.numero("Saldo", c -> celda(c.getSaldo())));
    }

    /** La fila 0 solo muestra el saldo inicial; las demás celdas quedan vacías. */
    private static String celda(BigDecimal valor) {
        return valor == null ? "" : Formato.dinero(valor);
    }
}
