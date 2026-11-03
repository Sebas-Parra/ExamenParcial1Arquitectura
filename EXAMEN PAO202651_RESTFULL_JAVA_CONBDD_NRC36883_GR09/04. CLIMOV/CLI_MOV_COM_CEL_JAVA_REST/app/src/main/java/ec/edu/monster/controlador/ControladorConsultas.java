package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.VistaConsultas;
import java.util.ArrayList;

/** Consulta de facturas y de la tabla de amortización de los créditos. */
public final class ControladorConsultas {
    private final ClienteRest cliente;
    private final EjecutorTareas ejecutor;
    private final VistaConsultas vista;

    public ControladorConsultas(ClienteRest cliente, EjecutorTareas ejecutor, VistaConsultas vista) {
        this.cliente = cliente;
        this.ejecutor = ejecutor;
        this.vista = vista;
    }

    /** Lista las facturas; la cédula puede quedar vacía para ver todas. Devuelve el error de validación o null. */
    public String buscarFacturas(String cedula) {
        String filtro = cedula == null ? "" : cedula.trim();
        if (!filtro.isEmpty() && !filtro.matches("\\d{10}")) return "La cédula debe tener 10 dígitos o quedar vacía.";
        LlamadaRest.ejecutar(ejecutor, vista, "Facturas", () -> cliente.listarFacturas(filtro),
            r -> vista.mostrarFacturas(r.getFacturas()));
        return null;
    }

    /** Muestra la factura; si es a crédito consulta también su tabla de amortización en el banco. */
    public void verFactura(Factura factura) {
        if (factura.getCodCredito() == null) {
            vista.mostrarFactura(null, factura, new ArrayList<CuotaAmortizacion>());
            return;
        }
        LlamadaRest.ejecutar(ejecutor, vista, "Tabla de amortización",
            () -> cliente.obtenerTablaAmortizacion(factura.getCodCredito()),
            r -> vista.mostrarFactura(null, factura, r.getTablaAmortizacion()));
    }

    /** Lista los créditos del cliente en el banco. Devuelve el error de validación o null. */
    public String buscarCreditos(String cedula) {
        String filtro = cedula == null ? "" : cedula.trim();
        if (!filtro.matches("\\d{10}")) return "La cédula debe tener 10 dígitos.";
        LlamadaRest.ejecutar(ejecutor, vista, "Créditos", () -> cliente.listarCreditos(filtro),
            r -> vista.mostrarCreditos(r.getNombre(), r.getCreditos()));
        return null;
    }

    public void verTabla(Credito credito) {
        LlamadaRest.ejecutar(ejecutor, vista, "Tabla de amortización",
            () -> cliente.obtenerTablaAmortizacion(credito.getCodCredito()),
            r -> vista.mostrarTabla(credito, r.getTablaAmortizacion()));
    }
}
