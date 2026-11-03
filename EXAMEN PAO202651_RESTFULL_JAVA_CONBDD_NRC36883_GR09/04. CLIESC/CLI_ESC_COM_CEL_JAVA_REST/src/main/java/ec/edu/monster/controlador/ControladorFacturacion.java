package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.DialogoFactura;
import ec.edu.monster.vista.Formato;
import ec.edu.monster.vista.PanelFacturacion;
import ec.edu.monster.vista.VentanaPrincipal;
import javax.swing.JOptionPane;

/** Venta de celulares en efectivo o con crédito directo del Banco BanQuito. */
public final class ControladorFacturacion {
    private final ClienteRest cliente;
    private final VentanaPrincipal ventana;
    private final PanelFacturacion panel;
    private final CarritoVenta carrito = new CarritoVenta();

    public ControladorFacturacion(ClienteRest cliente, VentanaPrincipal ventana) {
        this.cliente = cliente;
        this.ventana = ventana;
        this.panel = ventana.facturacion();
        panel.alAgregar(e -> agregar());
        panel.alQuitar(e -> quitar());
        panel.alVaciar(e -> { carrito.vaciar(); refrescar(); });
        panel.alCambiarFormaPago(e -> refrescar());
        panel.alFacturar(e -> facturar());
        refrescar();
    }

    public void cargarCelulares() {
        TareaRest.ejecutar(ventana, "Consultando celulares disponibles…", "Catálogo",
            () -> cliente.listarCelulares(false), r -> {
                panel.setCelulares(r.getCelulares());
                ventana.mostrarEstado(r.getCelulares().size() + " celulares disponibles para la venta.");
            });
    }

    private void agregar() {
        Celular celular = panel.celularSeleccionado();
        if (celular == null) {
            ventana.mostrarAviso("No hay celulares disponibles para agregar.", "Carrito", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        try {
            carrito.agregar(celular, panel.cantidad());
        } catch (IllegalArgumentException ex) {
            ventana.mostrarAviso(ex.getMessage(), "Carrito", JOptionPane.WARNING_MESSAGE);
        }
        refrescar();
    }

    private void quitar() {
        int linea = panel.lineaSeleccionada();
        if (linea < 0) {
            ventana.mostrarAviso("Selecciona en el carrito el celular que deseas quitar.", "Carrito",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        carrito.quitar(linea);
        refrescar();
    }

    private void refrescar() {
        String formaPago = panel.formaPago();
        panel.mostrarCarrito(carrito.lineas(), carrito.subtotal(), carrito.descuento(formaPago), carrito.total(formaPago));
    }

    private void facturar() {
        String error = validar();
        if (error != null) {
            ventana.mostrarAviso(error, "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String formaPago = panel.formaPago();
        boolean enEfectivo = CarritoVenta.EFECTIVO.equals(formaPago);
        String confirmacion = enEfectivo
            ? "Total a pagar en efectivo: " + Formato.dinero(carrito.total(formaPago))
                + "\n(descuento de " + Formato.dinero(carrito.descuento(formaPago)) + ")"
            : "Se solicitará al Banco BanQuito un crédito de " + Formato.dinero(carrito.total(formaPago))
                + " a " + panel.cuotas() + " meses.";
        if (!ventana.confirmar(confirmacion + "\n\n¿Confirmar la factura?", "Confirmar venta")) return;

        FacturaRequest solicitud = new FacturaRequest(panel.cedula(), enEfectivo ? panel.nombre() : null, formaPago,
            enEfectivo ? null : panel.cuotas(), carrito.detalles());
        TareaRest.ejecutar(ventana,
            enEfectivo ? "Registrando la factura…" : "Consultando al Banco BanQuito y registrando la factura…",
            enEfectivo ? "No se pudo facturar" : "Crédito no aprobado",
            () -> cliente.facturar(solicitud), r -> {
                carrito.vaciar();
                panel.limpiar();
                refrescar();
                DialogoFactura.mostrar(ventana, r.getMensaje(), r.getFactura(), r.getTablaAmortizacion());
            });
    }

    private String validar() {
        if (carrito.estaVacio()) return "Agrega al menos un celular al carrito.";
        if (!panel.cedula().matches("\\d{10}")) return "La cédula debe tener 10 dígitos.";
        if (CarritoVenta.EFECTIVO.equals(panel.formaPago()) && panel.nombre().isEmpty())
            return "Escribe el nombre del comprador.";
        return null;
    }
}
