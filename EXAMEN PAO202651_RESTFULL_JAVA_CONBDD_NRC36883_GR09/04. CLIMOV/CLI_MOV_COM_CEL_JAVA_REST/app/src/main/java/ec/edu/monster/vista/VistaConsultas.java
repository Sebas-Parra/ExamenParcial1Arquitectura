package ec.edu.monster.vista;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.Factura;
import java.util.List;

/** Lo que el controlador de consultas necesita de las pantallas de facturas y de amortización. */
public interface VistaConsultas extends VistaBase {
    void mostrarFacturas(List<Factura> facturas);

    void mostrarFactura(String mensaje, Factura factura, List<CuotaAmortizacion> tabla);

    void mostrarCreditos(String cliente, List<Credito> creditos);

    void mostrarTabla(Credito credito, List<CuotaAmortizacion> tabla);
}
