package ec.edu.monster.vista;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.Factura;
import java.util.List;

/** Muestra una factura con su detalle y, si es a crédito, la tabla de amortización. */
final class DialogoFactura {
    private DialogoFactura() { }

    /** mensaje es un aviso opcional (por ejemplo, tras facturar). */
    static void mostrar(Activity actividad, String mensaje, Factura f, List<CuotaAmortizacion> tabla) {
        View vista = actividad.getLayoutInflater().inflate(R.layout.dialogo_factura, null);
        TextView aviso = vista.findViewById(R.id.aviso);
        if (mensaje != null) {
            aviso.setText(mensaje);
            aviso.setVisibility(View.VISIBLE);
        }
        ((TextView) vista.findViewById(R.id.cliente)).setText(f.getNombre() + "\nCédula " + f.getCedula()
            + " · " + Formato.fecha(f.getFecha()) + "\n" + Formato.formaPago(f.getFormaPago()));

        LinearLayout detalles = vista.findViewById(R.id.detalles);
        for (DetalleFactura d : f.getDetalles()) {
            View fila = actividad.getLayoutInflater().inflate(R.layout.item_linea, detalles, false);
            ((TextView) fila.findViewById(R.id.nombre)).setText(d.getMarca() + " " + d.getModelo());
            ((TextView) fila.findViewById(R.id.detalle)).setText(d.getCantidad() + " × " + Formato.dinero(d.getPrecioUnitario()));
            ((TextView) fila.findViewById(R.id.subtotal)).setText(Formato.dinero(d.getSubtotal()));
            fila.findViewById(R.id.quitar).setVisibility(View.GONE);
            detalles.addView(fila);
        }

        TableLayout totales = vista.findViewById(R.id.totales);
        Tablas.total(totales, "Subtotal", Formato.dinero(f.getSubtotal()), false);
        boolean conDescuento = f.getDescuento() != null && f.getDescuento().signum() != 0;
        Tablas.total(totales, "EFECTIVO".equals(f.getFormaPago()) ? "Descuento efectivo (42%)" : "Descuento",
            (conDescuento ? "- " : "") + Formato.dinero(f.getDescuento()), false);
        Tablas.total(totales, "TOTAL", Formato.dinero(f.getTotal()), true);

        if (f.getCodCredito() != null) {
            vista.findViewById(R.id.tituloTabla).setVisibility(View.VISIBLE);
            TextView resumen = vista.findViewById(R.id.resumenTabla);
            resumen.setVisibility(View.VISIBLE);
            resumen.setText("Crédito BanQuito N.º " + f.getCodCredito() + " · " + f.getNumeroCuotas()
                + " cuotas de " + Formato.dinero(f.getValorCuota()) + " · 16.5% anual");
            Tablas.amortizacion(vista.findViewById(R.id.tabla), tabla, f.getTotal());
        }

        new MaterialAlertDialogBuilder(actividad)
            .setTitle("Factura N.º " + f.getCodFactura())
            .setView(vista)
            .setPositiveButton(R.string.cerrar, null)
            .show();
    }
}
