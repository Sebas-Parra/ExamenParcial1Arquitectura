package ec.edu.monster.vista;

import java.math.BigDecimal;
import java.util.Locale;

/** Formato de valores para mostrar en pantalla. */
public final class Formato {
    private Formato() { }

    public static String dinero(BigDecimal valor) {
        return valor == null ? "-" : String.format(Locale.ROOT, "$%,.2f", valor);
    }

    /** Fecha ISO del servidor (2026-11-01T10:30:00) en formato legible, sin segundos. */
    public static String fecha(String valor) {
        if (valor == null) return "-";
        String texto = valor.replace('T', ' ');
        return texto.length() > 16 ? texto.substring(0, 16) : texto;
    }

    public static String formaPago(String formaPago) {
        return "CREDITO".equals(formaPago) ? "Crédito directo" : "Efectivo";
    }
}
