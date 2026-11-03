package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.DialogoFactura;
import ec.edu.monster.vista.Formato;
import ec.edu.monster.vista.PanelAmortizacion;
import ec.edu.monster.vista.PanelFacturas;
import ec.edu.monster.vista.VentanaPrincipal;
import java.util.List;
import javax.swing.JOptionPane;

/** Consulta de facturas y de la tabla de amortización de los créditos. */
public final class ControladorConsultas {
    private final ClienteRest cliente;
    private final VentanaPrincipal ventana;
    private final PanelFacturas facturas;
    private final PanelAmortizacion amortizacion;

    public ControladorConsultas(ClienteRest cliente, VentanaPrincipal ventana) {
        this.cliente = cliente;
        this.ventana = ventana;
        this.facturas = ventana.facturas();
        this.amortizacion = ventana.amortizacion();
        facturas.alBuscar(e -> buscarFacturas());
        facturas.alVerDetalle(e -> verFactura());
        amortizacion.alBuscar(e -> buscarCreditos());
        amortizacion.alSeleccionarCredito(this::verTabla);
    }

    public void buscarFacturas() {
        String cedula = facturas.cedula();
        if (!cedula.isEmpty() && !cedula.matches("\\d{10}")) {
            ventana.mostrarAviso("La cédula debe tener 10 dígitos o quedar vacía.", "Facturas", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TareaRest.ejecutar(ventana, "Consultando facturas…", "Facturas", () -> cliente.listarFacturas(cedula), r -> {
            facturas.setFacturas(r.getFacturas());
            ventana.mostrarEstado(r.getFacturas().isEmpty() ? "No hay facturas registradas."
                : r.getFacturas().size() + " facturas encontradas.");
        });
    }

    private void verFactura() {
        Factura factura = facturas.seleccionada();
        if (factura == null) {
            ventana.mostrarAviso("Selecciona una factura.", "Facturas", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (factura.getCodCredito() == null) {
            DialogoFactura.mostrar(ventana, null, factura, List.of());
            return;
        }
        // Las facturas a crédito se muestran junto con su tabla de amortización consultada en el banco.
        TareaRest.ejecutar(ventana, "Consultando la tabla de amortización…", "Tabla de amortización",
            () -> cliente.obtenerTablaAmortizacion(factura.getCodCredito()),
            r -> DialogoFactura.mostrar(ventana, null, factura, r.getTablaAmortizacion()));
    }

    private void buscarCreditos() {
        String cedula = amortizacion.cedula();
        if (!cedula.matches("\\d{10}")) {
            ventana.mostrarAviso("La cédula debe tener 10 dígitos.", "Créditos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        TareaRest.ejecutar(ventana, "Consultando los créditos en el Banco BanQuito…", "Créditos",
            () -> cliente.listarCreditos(cedula), r -> {
                amortizacion.setCreditos(r.getNombre(), r.getCreditos());
                ventana.mostrarEstado(r.getCreditos().isEmpty() ? "El cliente no tiene créditos en el Banco BanQuito."
                    : r.getCreditos().size() + " créditos encontrados.");
            });
    }

    private void verTabla(Credito credito) {
        TareaRest.ejecutar(ventana, "Consultando la tabla de amortización…", "Tabla de amortización",
            () -> cliente.obtenerTablaAmortizacion(credito.getCodCredito()), r -> amortizacion.setTabla(
                "Crédito N.º " + credito.getCodCredito() + " · " + Formato.dinero(credito.getMonto()) + " a "
                    + credito.getNumeroCuotas() + " meses · tasa "
                    + (credito.getTasaAnual() == null ? "16.5" : credito.getTasaAnual().toPlainString()) + "% anual",
                r.getTablaAmortizacion(), credito.getMonto()));
    }
}
