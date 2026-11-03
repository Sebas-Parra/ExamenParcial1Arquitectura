package ec.edu.monster.vista;

import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorConsultas;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.Factura;
import java.util.List;

/** Pantallas de Facturas y de Tabla de amortización; comparten el controlador de consultas. */
final class PantallaConsultas implements VistaConsultas {
    private final PrincipalActivity actividad;
    private final View facturas;
    private final View amortizacion;
    private ControladorConsultas controlador;

    PantallaConsultas(PrincipalActivity actividad) {
        this.actividad = actividad;
        facturas = actividad.getLayoutInflater().inflate(R.layout.pantalla_busqueda, null);
        amortizacion = actividad.getLayoutInflater().inflate(R.layout.pantalla_busqueda, null);
        ((TextInputLayout) facturas.findViewById(R.id.grupoCedula)).setHint(actividad.getString(R.string.cedula_filtro));
        ((TextInputLayout) amortizacion.findViewById(R.id.grupoCedula)).setHint(actividad.getString(R.string.cedula_cliente));
    }

    void conectar(ControladorConsultas controlador) {
        this.controlador = controlador;
        alBuscar(facturas, () -> validar(controlador.buscarFacturas(cedula(facturas))));
        alBuscar(amortizacion, () -> validar(controlador.buscarCreditos(cedula(amortizacion))));
    }

    View vistaFacturas() { return facturas; }
    View vistaAmortizacion() { return amortizacion; }

    void cargarFacturas() {
        validar(controlador.buscarFacturas(cedula(facturas)));
    }

    private void alBuscar(View pantalla, Runnable accion) {
        pantalla.findViewById(R.id.buscar).setOnClickListener(v -> accion.run());
        ((EditText) pantalla.findViewById(R.id.cedula)).setOnEditorActionListener((v, id, evento) -> {
            if (id != EditorInfo.IME_ACTION_SEARCH) return false;
            accion.run();
            return true;
        });
    }

    private static String cedula(View pantalla) {
        return ((EditText) pantalla.findViewById(R.id.cedula)).getText().toString();
    }

    private void validar(String error) {
        if (error != null) mostrarAviso("Datos incompletos", error);
    }

    @Override public void setOcupado(boolean ocupado) { actividad.setOcupado(ocupado); }
    @Override public void mostrarAviso(String titulo, String mensaje) { actividad.mostrarAviso(titulo, mensaje); }

    @Override
    public void mostrarFacturas(List<Factura> lista) {
        LinearLayout contenedor = facturas.findViewById(R.id.lista);
        contenedor.removeAllViews();
        if (lista.isEmpty()) contenedor.addView(actividad.textoVacio(R.string.sin_facturas));
        for (Factura f : lista) {
            View fila = actividad.getLayoutInflater().inflate(R.layout.item_resultado, contenedor, false);
            ((TextView) fila.findViewById(R.id.titulo)).setText("Factura N.º " + f.getCodFactura() + " · " + f.getNombre());
            ((TextView) fila.findViewById(R.id.detalle)).setText(Formato.fecha(f.getFecha()) + " · Cédula " + f.getCedula()
                + (f.getCodCredito() == null ? "" : "\nCrédito N.º " + f.getCodCredito() + " · " + f.getNumeroCuotas() + " cuotas"));
            ((TextView) fila.findViewById(R.id.valor)).setText(Formato.dinero(f.getTotal()));
            etiqueta(fila, Formato.formaPago(f.getFormaPago()), f.getCodCredito() != null);
            fila.setOnClickListener(v -> controlador.verFactura(f));
            contenedor.addView(fila);
        }
    }

    @Override
    public void mostrarFactura(String mensaje, Factura factura, List<CuotaAmortizacion> tabla) {
        DialogoFactura.mostrar(actividad, mensaje, factura, tabla);
    }

    @Override
    public void mostrarCreditos(String cliente, List<Credito> creditos) {
        TextView encabezado = amortizacion.findViewById(R.id.encabezado);
        TextView ayuda = amortizacion.findViewById(R.id.ayuda);
        encabezado.setVisibility(View.VISIBLE);
        encabezado.setText(cliente == null ? "Créditos del cliente" : "Créditos de " + cliente);
        ayuda.setVisibility(View.VISIBLE);
        ayuda.setText(creditos.isEmpty() ? R.string.sin_creditos : R.string.elegir_credito);
        amortizacion.findViewById(R.id.tarjetaTabla).setVisibility(View.GONE);
        LinearLayout contenedor = amortizacion.findViewById(R.id.lista);
        contenedor.removeAllViews();
        for (Credito c : creditos) {
            View fila = actividad.getLayoutInflater().inflate(R.layout.item_resultado, contenedor, false);
            ((TextView) fila.findViewById(R.id.titulo)).setText("Crédito N.º " + c.getCodCredito());
            ((TextView) fila.findViewById(R.id.detalle)).setText(c.getFecha() + " · " + c.getNumeroCuotas()
                + " cuotas de " + Formato.dinero(c.getValorCuota()));
            ((TextView) fila.findViewById(R.id.valor)).setText(Formato.dinero(c.getMonto()));
            etiqueta(fila, c.getEstado(), "ACTIVO".equals(c.getEstado()));
            fila.setOnClickListener(v -> {
                marcar(contenedor, fila);
                controlador.verTabla(c);
            });
            contenedor.addView(fila);
        }
    }

    @Override
    public void mostrarTabla(Credito credito, List<CuotaAmortizacion> tabla) {
        amortizacion.findViewById(R.id.tarjetaTabla).setVisibility(View.VISIBLE);
        ((TextView) amortizacion.findViewById(R.id.resumenTabla)).setText("Crédito N.º " + credito.getCodCredito()
            + " · " + Formato.dinero(credito.getMonto()) + " a " + credito.getNumeroCuotas() + " meses · tasa "
            + (credito.getTasaAnual() == null ? "16.5" : credito.getTasaAnual().toPlainString()) + "% anual");
        Tablas.amortizacion(amortizacion.findViewById(R.id.tabla), tabla, credito.getMonto());
    }

    private void etiqueta(View fila, String texto, boolean resaltada) {
        TextView etiqueta = fila.findViewById(R.id.etiqueta);
        etiqueta.setText(texto);
        etiqueta.setTextColor(resaltada ? 0xFF17633E : ContextCompat.getColor(actividad, R.color.gris));
    }

    /** Resalta la tarjeta del crédito elegido. */
    private void marcar(LinearLayout contenedor, View elegida) {
        for (int i = 0; i < contenedor.getChildCount(); i++) {
            MaterialCardView tarjeta = (MaterialCardView) contenedor.getChildAt(i);
            boolean activa = tarjeta == elegida;
            tarjeta.setStrokeColor(ContextCompat.getColor(actividad, activa ? R.color.azul_marino : R.color.borde));
            tarjeta.setStrokeWidth((int) ((activa ? 2 : 1) * actividad.getResources().getDisplayMetrics().density));
        }
    }
}
