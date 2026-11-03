package ec.edu.monster.vista;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorFacturacion;
import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.Factura;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Pantalla de venta: carrito, forma de pago, datos del comprador y totales. */
final class PantallaFacturacion implements VistaFacturacion {
    private static final int CUOTAS_PREDETERMINADAS = 6;

    private final PrincipalActivity actividad;
    private final View raiz;
    private final Spinner celulares;
    private final EditText cantidad;
    private final LinearLayout carrito;
    private final TextView subtotal;
    private final TextView descuento;
    private final TextView total;
    private final RadioGroup formaPago;
    private final EditText cedula;
    private final EditText nombre;
    private final Spinner cuotas;
    private final List<Celular> disponibles = new ArrayList<>();
    private ControladorFacturacion controlador;

    PantallaFacturacion(PrincipalActivity actividad) {
        this.actividad = actividad;
        raiz = actividad.getLayoutInflater().inflate(R.layout.pantalla_facturacion, null);
        celulares = raiz.findViewById(R.id.celulares);
        cantidad = raiz.findViewById(R.id.cantidad);
        carrito = raiz.findViewById(R.id.carrito);
        subtotal = raiz.findViewById(R.id.subtotal);
        descuento = raiz.findViewById(R.id.descuento);
        total = raiz.findViewById(R.id.total);
        formaPago = raiz.findViewById(R.id.formaPago);
        cedula = raiz.findViewById(R.id.cedula);
        nombre = raiz.findViewById(R.id.nombre);
        cuotas = raiz.findViewById(R.id.cuotas);

        List<String> plazos = new ArrayList<>();
        for (int n = 3; n <= 18; n++) plazos.add(n + " meses");
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(actividad, R.layout.item_selector, plazos);
        adaptador.setDropDownViewResource(R.layout.item_selector_desplegable);
        cuotas.setAdapter(adaptador);
        cuotas.setSelection(CUOTAS_PREDETERMINADAS - 3);
    }

    void conectar(ControladorFacturacion controlador) {
        this.controlador = controlador;
        raiz.findViewById(R.id.agregar).setOnClickListener(v -> agregar());
        raiz.findViewById(R.id.vaciar).setOnClickListener(v -> controlador.vaciar());
        formaPago.setOnCheckedChangeListener((grupo, id) -> {
            boolean credito = id == R.id.credito;
            raiz.findViewById(R.id.grupoNombre).setVisibility(credito ? View.GONE : View.VISIBLE);
            raiz.findViewById(R.id.grupoCuotas).setVisibility(credito ? View.VISIBLE : View.GONE);
            ((TextView) raiz.findViewById(R.id.nota)).setText(credito ? R.string.nota_credito : R.string.nota_efectivo);
            controlador.cambiarFormaPago(credito ? CarritoVenta.CREDITO : CarritoVenta.EFECTIVO);
        });
        raiz.findViewById(R.id.facturar).setOnClickListener(v -> facturar());
    }

    View vista() {
        return raiz;
    }

    private void agregar() {
        int posicion = celulares.getSelectedItemPosition();
        if (posicion < 0 || posicion >= disponibles.size()) {
            mostrarAviso("Carrito", "No hay celulares disponibles para agregar.");
            return;
        }
        int unidades;
        try {
            unidades = Integer.parseInt(cantidad.getText().toString().trim());
        } catch (NumberFormatException ex) {
            unidades = 0;
        }
        controlador.agregar(disponibles.get(posicion), unidades);
    }

    private void facturar() {
        boolean credito = formaPago.getCheckedRadioButtonId() == R.id.credito;
        String confirmacion = credito
            ? "Se solicitará al Banco BanQuito un crédito de " + total.getText() + " a "
                + (cuotas.getSelectedItemPosition() + 3) + " meses."
            : "Total a pagar en efectivo: " + total.getText() + ".";
        new MaterialAlertDialogBuilder(actividad)
            .setTitle("Confirmar venta")
            .setMessage(confirmacion + "\n\n¿Confirmar la factura?")
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.facturar, (d, b) -> {
                String error = controlador.facturar(cedula.getText().toString(), nombre.getText().toString(),
                    cuotas.getSelectedItemPosition() + 3);
                if (error != null) mostrarAviso("Datos incompletos", error);
            })
            .show();
    }

    @Override public void setOcupado(boolean ocupado) { actividad.setOcupado(ocupado); }
    @Override public void mostrarAviso(String titulo, String mensaje) { actividad.mostrarAviso(titulo, mensaje); }

    @Override
    public void setCelulares(List<Celular> lista) {
        disponibles.clear();
        disponibles.addAll(lista);
        List<String> etiquetas = new ArrayList<>();
        for (Celular c : lista) etiquetas.add(c.nombre() + " · " + Formato.dinero(c.getPrecio()));
        ArrayAdapter<String> adaptador = new ArrayAdapter<>(actividad, R.layout.item_selector, etiquetas);
        adaptador.setDropDownViewResource(R.layout.item_selector_desplegable);
        celulares.setAdapter(adaptador);
    }

    @Override
    public void mostrarCarrito(List<CarritoVenta.Linea> lineas, BigDecimal sub, BigDecimal desc, BigDecimal tot) {
        carrito.removeAllViews();
        if (lineas.isEmpty()) carrito.addView(actividad.textoVacio(R.string.carrito_vacio));
        for (int i = 0; i < lineas.size(); i++) {
            CarritoVenta.Linea linea = lineas.get(i);
            View fila = actividad.getLayoutInflater().inflate(R.layout.item_linea, carrito, false);
            ((TextView) fila.findViewById(R.id.nombre)).setText(linea.celular().nombre());
            ((TextView) fila.findViewById(R.id.detalle)).setText(linea.cantidad() + " × " + Formato.dinero(linea.celular().getPrecio()));
            ((TextView) fila.findViewById(R.id.subtotal)).setText(Formato.dinero(linea.subtotal()));
            int indice = i;
            fila.findViewById(R.id.quitar).setOnClickListener(v -> controlador.quitar(indice));
            carrito.addView(fila);
        }
        subtotal.setText(Formato.dinero(sub));
        descuento.setText(desc.signum() == 0 ? Formato.dinero(desc) : "- " + Formato.dinero(desc));
        total.setText(Formato.dinero(tot));
        cantidad.setText("1");
    }

    @Override
    public void limpiarFormulario() {
        cedula.setText("");
        nombre.setText("");
        cuotas.setSelection(CUOTAS_PREDETERMINADAS - 3);
        formaPago.check(R.id.efectivo);
    }

    @Override
    public void mostrarFactura(String mensaje, Factura factura, List<CuotaAmortizacion> tabla) {
        DialogoFactura.mostrar(actividad, mensaje, factura, tabla);
    }
}
