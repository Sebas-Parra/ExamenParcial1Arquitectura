package ec.edu.monster.vista;

import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.Factura;
import java.math.BigDecimal;
import java.util.List;

/** Lo que el controlador de facturación necesita de la pantalla. */
public interface VistaFacturacion extends VistaBase {
    void setCelulares(List<Celular> celulares);

    void mostrarCarrito(List<CarritoVenta.Linea> lineas, BigDecimal subtotal, BigDecimal descuento, BigDecimal total);

    void limpiarFormulario();

    void mostrarFactura(String mensaje, Factura factura, List<CuotaAmortizacion> tabla);
}
