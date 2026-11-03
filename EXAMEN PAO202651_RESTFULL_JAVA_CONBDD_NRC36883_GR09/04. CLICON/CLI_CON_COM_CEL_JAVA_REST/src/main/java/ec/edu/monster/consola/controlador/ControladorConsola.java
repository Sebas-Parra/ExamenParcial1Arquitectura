package ec.edu.monster.consola.controlador;

import ec.edu.monster.cliente.ClienteRest;
import ec.edu.monster.consola.vista.VistaConsola;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.modelo.RespuestaRest;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Coordina la navegación de consola y las llamadas al API REST de la comercializadora. */
public final class ControladorConsola {
    private static final BigDecimal DESCUENTO_EFECTIVO = new BigDecimal("0.42");
    private static final List<String> FORMAS_PAGO = List.of("EFECTIVO", "CREDITO");

    private final ClienteRest cliente;
    private final VistaConsola vista;

    public ControladorConsola(ClienteRest cliente, VistaConsola vista) {
        this.cliente = cliente;
        this.vista = vista;
    }

    public void ejecutar() {
        vista.mostrarBienvenida();
        try {
            while (true) {
                vista.mostrarMenuPrincipal();
                String opcion = vista.leer("Opción: ");
                boolean esperar = true;
                try {
                    switch (opcion) {
                        case "1" -> consultarCatalogo();
                        case "2" -> registrarCelular();
                        case "3" -> esperar = actualizarCelular();
                        case "4" -> esperar = retirarCelular();
                        case "5" -> esperar = facturar();
                        case "6" -> consultarFacturas();
                        case "7" -> esperar = consultarAmortizacion();
                        case "0" -> {
                            vista.mostrarMensaje("Hasta luego.");
                            return;
                        }
                        default -> vista.mostrarMensaje("Opción inválida. Escriba una opción del menú, del 0 al 7.");
                    }
                } catch (IOException e) {
                    vista.mostrarError(e.getMessage());
                    if (Thread.currentThread().isInterrupted()) return;
                }
                if (esperar) vista.esperarParaContinuar();
            }
        } catch (VistaConsola.FinEntrada e) {
            vista.mostrarMensaje("\nHasta luego.");
        } catch (IOException e) {
            vista.mostrarError("de lectura: " + e.getMessage());
        }
    }

    private void consultarCatalogo() throws IOException {
        vista.mostrarSeccion("CATÁLOGO DE CELULARES");
        RespuestaRest respuesta = cliente.listarCelulares(false);
        if (respuesta.isExito()) vista.mostrarCatalogo(respuesta.getCelulares());
        else vista.mostrarRespuesta(respuesta);
    }

    private void registrarCelular() throws IOException {
        vista.mostrarSeccion("REGISTRAR CELULAR");
        Celular celular = new Celular(vista.leerTexto("Marca", null, true), vista.leerTexto("Modelo", null, true),
            vacioANulo(vista.leerTexto("Descripción (opcional)", null, false)), vista.leerPrecio(null));
        RespuestaRest respuesta = cliente.crearCelular(celular);
        vista.mostrarRespuesta(respuesta);
        if (respuesta.isExito()) vista.mostrarCelular(respuesta.getCelular());
    }

    private boolean actualizarCelular() throws IOException {
        vista.mostrarSeccion("ACTUALIZAR CELULAR");
        RespuestaRest catalogo = cliente.listarCelulares(true);
        if (!catalogo.isExito()) {
            vista.mostrarRespuesta(catalogo);
            return true;
        }
        Celular actual = vista.seleccionar("Celular a actualizar", catalogo.getCelulares(),
            ControladorConsola::etiquetaCelular, "Volver al menú principal");
        if (actual == null) return false;
        vista.mostrarSeccion("ACTUALIZAR CELULAR | " + actual.nombre());
        vista.mostrarMensaje("Presione Enter para conservar el valor actual.");
        vista.mostrarMensaje("");
        Celular cambios = new Celular(vista.leerTexto("Marca", actual.getMarca(), true),
            vista.leerTexto("Modelo", actual.getModelo(), true),
            vacioANulo(vista.leerTexto("Descripción", actual.getDescripcion(), false)),
            vista.leerPrecio(actual.getPrecio()));
        if (Boolean.FALSE.equals(actual.getDisponible()) && vista.confirmar("El celular está retirado. ¿Volver a ofrecerlo?"))
            cambios.setDisponible(true);
        RespuestaRest respuesta = cliente.actualizarCelular(actual.getCodCelular(), cambios);
        vista.mostrarRespuesta(respuesta);
        if (respuesta.isExito()) vista.mostrarCelular(respuesta.getCelular());
        return true;
    }

    private boolean retirarCelular() throws IOException {
        vista.mostrarSeccion("RETIRAR CELULAR DEL CATÁLOGO");
        RespuestaRest catalogo = cliente.listarCelulares(false);
        if (!catalogo.isExito()) {
            vista.mostrarRespuesta(catalogo);
            return true;
        }
        Celular celular = vista.seleccionar("Celular a retirar", catalogo.getCelulares(),
            ControladorConsola::etiquetaCelular, "Volver al menú principal");
        if (celular == null) return false;
        if (!vista.confirmar("¿Retirar " + celular.nombre() + " del catálogo? Sus facturas se conservan")) return false;
        vista.mostrarRespuesta(cliente.eliminarCelular(celular.getCodCelular()));
        return true;
    }

    private boolean facturar() throws IOException {
        vista.mostrarSeccion("FACTURAR VENTA | Paso 1/3: Celulares");
        RespuestaRest catalogo = cliente.listarCelulares(false);
        if (!catalogo.isExito()) {
            vista.mostrarRespuesta(catalogo);
            return true;
        }
        Map<Celular, Integer> carrito = new LinkedHashMap<>();
        while (true) {
            Celular celular = vista.seleccionar("Celular a vender", catalogo.getCelulares(),
                ControladorConsola::etiquetaCelular, carrito.isEmpty() ? "Volver al menú principal" : "Terminar de agregar");
            if (celular == null) break;
            int cantidad = vista.leerEntero("Cantidad: ", 1, 100);
            carrito.merge(celular, cantidad, Integer::sum);
            vista.mostrarSeccion("FACTURAR VENTA | Paso 1/3: Celulares");
            vista.mostrarCarrito(carrito);
            if (!vista.confirmar("¿Agregar otro celular?")) break;
        }
        if (carrito.isEmpty()) return false;

        vista.mostrarSeccion("FACTURAR VENTA | Paso 2/3: Forma de pago");
        vista.mostrarCarrito(carrito);
        String formaPago = vista.seleccionar("Forma de pago", FORMAS_PAGO,
            f -> f.equals("EFECTIVO") ? "Efectivo (42% de descuento)" : "Crédito directo Banco BanQuito (3 a 18 meses)",
            "Cancelar la venta");
        if (formaPago == null) return false;

        vista.mostrarSeccion("FACTURAR VENTA | Paso 3/3: Comprador");
        vista.mostrarCarrito(carrito);
        vista.mostrarMensaje("");
        String cedula = vista.leerCedula("Cédula del comprador: ", false);
        String nombre = null;
        Integer cuotas = null;
        BigDecimal subtotal = carrito.entrySet().stream()
            .map(l -> l.getKey().getPrecio().multiply(BigDecimal.valueOf(l.getValue())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (formaPago.equals("EFECTIVO")) {
            nombre = vista.leerTexto("Nombre del comprador", null, true);
            BigDecimal descuento = subtotal.multiply(DESCUENTO_EFECTIVO).setScale(2, RoundingMode.HALF_UP);
            vista.mostrarMensaje("");
            vista.mostrarMensaje("Subtotal: " + VistaConsola.dinero(subtotal) + "  Descuento 42%: "
                + VistaConsola.dinero(descuento) + "  Total a pagar: " + VistaConsola.dinero(subtotal.subtract(descuento)));
        } else {
            vista.mostrarMensaje("El banco verificará si el comprador es sujeto de crédito y su monto máximo.");
            cuotas = vista.leerEntero("Número de cuotas mensuales (3-18): ", 3, 18);
            vista.mostrarMensaje("");
            vista.mostrarMensaje("Valor a financiar: " + VistaConsola.dinero(subtotal) + " a " + cuotas
                + " meses (16.5% anual)");
        }
        if (!vista.confirmar("¿Confirmar la factura?")) {
            vista.mostrarMensaje("Venta cancelada.");
            return true;
        }

        List<DetalleFactura> detalles = carrito.entrySet().stream()
            .map(l -> new DetalleFactura(l.getKey().getCodCelular(), l.getValue())).toList();
        RespuestaRest respuesta = cliente.facturar(new FacturaRequest(cedula, nombre, formaPago, cuotas, detalles));
        vista.mostrarSeccion("FACTURAR VENTA | Resultado");
        vista.mostrarRespuesta(respuesta);
        if (respuesta.isExito() && respuesta.getFactura() != null) {
            vista.mostrarFactura(respuesta.getFactura());
            if (!respuesta.getTablaAmortizacion().isEmpty()) {
                vista.mostrarMensaje("");
                vista.mostrarMensaje("TABLA DE AMORTIZACIÓN");
                vista.mostrarTablaAmortizacion(respuesta);
            }
        }
        return true;
    }

    private void consultarFacturas() throws IOException {
        vista.mostrarSeccion("CONSULTAR FACTURAS");
        String cedula = vista.leerCedula("Cédula del comprador (Enter para todas): ", true);
        RespuestaRest respuesta = cliente.listarFacturas(cedula);
        if (!respuesta.isExito()) {
            vista.mostrarRespuesta(respuesta);
            return;
        }
        if (respuesta.getFacturas().isEmpty()) {
            vista.mostrarMensaje("");
            vista.mostrarMensaje("No hay facturas registradas" + (cedula.isEmpty() ? "." : " para esa cédula."));
            return;
        }
        Factura factura = vista.seleccionar("Facturas (elija una para ver el detalle)", respuesta.getFacturas(),
            vista::resumenFactura, "Volver al menú principal");
        if (factura != null) vista.mostrarFactura(factura);
    }

    private boolean consultarAmortizacion() throws IOException {
        vista.mostrarSeccion("TABLA DE AMORTIZACIÓN");
        String cedula = vista.leerCedula("Cédula del cliente: ", false);
        RespuestaRest creditos = cliente.listarCreditos(cedula);
        if (!creditos.isExito()) {
            vista.mostrarRespuesta(creditos);
            return true;
        }
        if (creditos.getCreditos().isEmpty()) {
            vista.mostrarMensaje("");
            vista.mostrarMensaje("El cliente no tiene créditos en el Banco BanQuito.");
            return true;
        }
        if (creditos.getNombre() != null) vista.mostrarMensaje("Cliente: " + creditos.getNombre());
        Credito credito = vista.seleccionar("Créditos del cliente", creditos.getCreditos(),
            ControladorConsola::etiquetaCredito, "Volver al menú principal");
        if (credito == null) return false;
        RespuestaRest tabla = cliente.obtenerTablaAmortizacion(credito.getCodCredito());
        vista.mostrarSeccion("TABLA DE AMORTIZACIÓN | Crédito N.º " + credito.getCodCredito());
        if (tabla.isExito()) vista.mostrarTablaAmortizacion(tabla);
        else vista.mostrarRespuesta(tabla);
        return true;
    }

    private static String etiquetaCelular(Celular c) {
        return c.nombre() + " - " + VistaConsola.dinero(c.getPrecio())
            + (Boolean.FALSE.equals(c.getDisponible()) ? " (retirado)" : "");
    }

    private static String etiquetaCredito(Credito c) {
        return "Crédito N.º " + c.getCodCredito() + " | " + VistaConsola.dinero(c.getMonto()) + " | "
            + c.getNumeroCuotas() + " cuotas de " + VistaConsola.dinero(c.getValorCuota()) + " | "
            + c.getFecha() + " | " + c.getEstado();
    }

    private static String vacioANulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto;
    }
}
