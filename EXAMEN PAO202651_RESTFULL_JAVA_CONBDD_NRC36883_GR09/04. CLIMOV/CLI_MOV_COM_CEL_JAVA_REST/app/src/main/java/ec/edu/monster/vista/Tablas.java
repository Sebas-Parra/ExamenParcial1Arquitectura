package ec.edu.monster.vista;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import ec.edu.monster.R;
import ec.edu.monster.modelo.CuotaAmortizacion;
import java.util.List;

/** Construye las tablas de texto de las pantallas (amortización y totales). */
final class Tablas {
    private Tablas() { }

    /** Tabla de amortización con las columnas del enunciado, empezando por la fila 0 (saldo inicial). */
    static void amortizacion(TableLayout tabla, List<CuotaAmortizacion> lista, java.math.BigDecimal monto) {
        Context contexto = tabla.getContext();
        List<CuotaAmortizacion> cuotas = CuotaAmortizacion.conFilaInicial(lista, monto);
        tabla.removeAllViews();
        TableRow cabecera = new TableRow(contexto);
        cabecera.setBackgroundColor(ContextCompat.getColor(contexto, R.color.suave));
        for (String titulo : new String[] {"#", "Cuota", "Interés", "Capital", "Saldo"})
            cabecera.addView(celda(contexto, titulo, true, R.color.azul_marino));
        tabla.addView(cabecera);
        for (int i = 0; i < cuotas.size(); i++) {
            CuotaAmortizacion c = cuotas.get(i);
            TableRow fila = new TableRow(contexto);
            if (i % 2 == 1) fila.setBackgroundColor(0xFFF7F9FC);
            fila.addView(celda(contexto, String.valueOf(c.getNumeroCuota()), false, R.color.tinta));
            fila.addView(celda(contexto, dinero(c.getValorCuota()), false, R.color.tinta));
            fila.addView(celda(contexto, dinero(c.getInteresPagado()), false, R.color.tinta));
            fila.addView(celda(contexto, dinero(c.getCapitalPagado()), false, R.color.tinta));
            fila.addView(celda(contexto, dinero(c.getSaldo()), false, R.color.tinta));
            tabla.addView(fila);
        }
    }

    /** Fila "etiqueta ........ valor" para el resumen de totales. */
    static void total(TableLayout tabla, String etiqueta, String valor, boolean destacado) {
        Context contexto = tabla.getContext();
        TableRow fila = new TableRow(contexto);
        TextView izquierda = celda(contexto, etiqueta, destacado, destacado ? R.color.tinta : R.color.gris);
        izquierda.setGravity(Gravity.START);
        TextView derecha = celda(contexto, valor, destacado, destacado ? R.color.azul_marino : R.color.tinta);
        if (destacado) {
            izquierda.setTextSize(16);
            derecha.setTextSize(17);
        }
        fila.addView(izquierda);
        fila.addView(derecha);
        tabla.addView(fila);
    }

    /** La fila 0 solo muestra el saldo; las celdas sin valor quedan vacías. */
    private static String dinero(java.math.BigDecimal valor) {
        return valor == null ? "" : Formato.dinero(valor);
    }

    private static TextView celda(Context contexto, String texto, boolean negrita, int color) {
        TextView celda = new TextView(contexto);
        celda.setText(texto);
        celda.setTextSize(13);
        celda.setTextColor(ContextCompat.getColor(contexto, color));
        celda.setGravity(Gravity.END);
        int horizontal = (int) (6 * contexto.getResources().getDisplayMetrics().density);
        int vertical = (int) (7 * contexto.getResources().getDisplayMetrics().density);
        celda.setPadding(horizontal, vertical, horizontal, vertical);
        if (negrita) celda.setTypeface(celda.getTypeface(), Typeface.BOLD);
        return celda;
    }
}
