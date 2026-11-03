package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.VistaFacturacion;

/** Venta de celulares en efectivo o con crédito directo del Banco BanQuito. */
public final class ControladorFacturacion {
    private final ClienteRest cliente;
    private final EjecutorTareas ejecutor;
    private final VistaFacturacion vista;
    private final CarritoVenta carrito = new CarritoVenta();
    private String formaPago = CarritoVenta.EFECTIVO;

    public ControladorFacturacion(ClienteRest cliente, EjecutorTareas ejecutor, VistaFacturacion vista) {
        this.cliente = cliente;
        this.ejecutor = ejecutor;
        this.vista = vista;
    }

    public void cargarCelulares() {
        LlamadaRest.ejecutar(ejecutor, vista, "Catálogo", () -> cliente.listarCelulares(false),
            r -> vista.setCelulares(r.getCelulares()));
        refrescar();
    }

    public void agregar(Celular celular, int cantidad) {
        try {
            carrito.agregar(celular, cantidad);
        } catch (IllegalArgumentException ex) {
            vista.mostrarAviso("Carrito", ex.getMessage());
        }
        refrescar();
    }

    public void quitar(int linea) {
        if (linea >= 0 && linea < carrito.lineas().size()) carrito.quitar(linea);
        refrescar();
    }

    public void vaciar() {
        carrito.vaciar();
        refrescar();
    }

    public void cambiarFormaPago(String forma) {
        formaPago = CarritoVenta.CREDITO.equals(forma) ? CarritoVenta.CREDITO : CarritoVenta.EFECTIVO;
        refrescar();
    }

    /** Valida y envía la factura; devuelve el error de validación o null si se envió al servidor. */
    public String facturar(String cedula, String nombre, int cuotas) {
        boolean enEfectivo = CarritoVenta.EFECTIVO.equals(formaPago);
        String cedulaLimpia = cedula == null ? "" : cedula.trim();
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (carrito.estaVacio()) return "Agrega al menos un celular al carrito.";
        if (!cedulaLimpia.matches("\\d{10}")) return "La cédula debe tener 10 dígitos.";
        if (enEfectivo && nombreLimpio.isEmpty()) return "Escribe el nombre del comprador.";
        if (!enEfectivo && (cuotas < 3 || cuotas > 18)) return "El número de cuotas debe estar entre 3 y 18.";

        FacturaRequest solicitud = new FacturaRequest(cedulaLimpia, enEfectivo ? nombreLimpio : null, formaPago,
            enEfectivo ? null : cuotas, carrito.detalles());
        LlamadaRest.ejecutar(ejecutor, vista, enEfectivo ? "No se pudo facturar" : "Crédito no aprobado",
            () -> cliente.facturar(solicitud), r -> {
                carrito.vaciar();
                formaPago = CarritoVenta.EFECTIVO;
                vista.limpiarFormulario();
                refrescar();
                vista.mostrarFactura(r.getMensaje(), r.getFactura(), r.getTablaAmortizacion());
            });
        return null;
    }

    public CarritoVenta carrito() {
        return carrito;
    }

    private void refrescar() {
        vista.mostrarCarrito(carrito.lineas(), carrito.subtotal(), carrito.descuento(formaPago), carrito.total(formaPago));
    }
}
