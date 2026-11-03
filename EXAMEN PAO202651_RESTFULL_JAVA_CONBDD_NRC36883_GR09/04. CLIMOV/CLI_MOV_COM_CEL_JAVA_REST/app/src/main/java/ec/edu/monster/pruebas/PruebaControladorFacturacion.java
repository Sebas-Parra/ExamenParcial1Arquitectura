package ec.edu.monster.pruebas;

import ec.edu.monster.controlador.ControladorFacturacion;
import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.VistaFacturacion;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/** Flujo de venta del controlador con una vista falsa y llamadas síncronas, sin Android. */
final class PruebaControladorFacturacion {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            VistaFalsa vista = new VistaFalsa();
            ControladorFacturacion controlador = new ControladorFacturacion(
                new ClienteRest(servidor.endpoint()), new EjecutorSincrono(), vista);
            Celular a55 = new Celular("Samsung", "Galaxy A55", null, new BigDecimal("450.00"));
            a55.setCodCelular(4);

            SoportePruebasRest.exigir(controlador.facturar("1712345678", "Ana", 6) != null,
                "No se debe facturar con el carrito vacío");
            controlador.agregar(a55, 1);
            SoportePruebasRest.exigir(new BigDecimal("261.00").equals(vista.total),
                "En efectivo el total de 450.00 debe ser 261.00 y fue " + vista.total);
            controlador.cambiarFormaPago(CarritoVenta.CREDITO);
            SoportePruebasRest.exigir(new BigDecimal("450.00").equals(vista.total), "En crédito no hay descuento");
            SoportePruebasRest.exigir("La cédula debe tener 10 dígitos.".equals(controlador.facturar("123", null, 6)),
                "Debe validar la cédula antes de llamar al servidor");
            SoportePruebasRest.exigir(servidor.solicitudes.isEmpty(), "Una validación fallida no debe llamar al servidor");

            servidor.responder(400, "{\"estado\":\"ERROR\",\"codigo\":\"CRED-001\",\"mensaje\":\"El comprador no es sujeto de credito\","
                + "\"motivo\":\"Los clientes de genero masculino deben tener al menos 25 anios\"}");
            SoportePruebasRest.exigir(controlador.facturar("1734567890", null, 6) == null, "Debe enviar la factura");
            SoportePruebasRest.exigir("Crédito no aprobado".equals(vista.tituloAviso) && vista.mensajeAviso.contains("25 anios")
                && !controlador.carrito().estaVacio(), "El rechazo debe mostrar el motivo y conservar el carrito");
            SoportePruebasRest.exigir(servidor.cuerpo.contains("\"numeroCuotas\":6"), "Debe enviar el número de cuotas");

            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Factura registrada con credito directo\","
                + "\"factura\":{\"codFactura\":4,\"formaPago\":\"CREDITO\",\"total\":450.00,\"codCredito\":2},"
                + "\"tablaAmortizacion\":[{\"numeroCuota\":1,\"valorCuota\":78.65,\"interesPagado\":6.19,"
                + "\"capitalPagado\":72.46,\"saldo\":377.54}]}");
            SoportePruebasRest.exigir(controlador.facturar("1712345678", null, 6) == null, "Debe enviar la factura");
            SoportePruebasRest.exigir(vista.factura != null && vista.factura.getCodCredito() == 2 && vista.tabla.size() == 1,
                "Debe mostrar la factura con su tabla de amortización");
            SoportePruebasRest.exigir(controlador.carrito().estaVacio() && vista.limpiado
                && BigDecimal.ZERO.setScale(2).equals(vista.total), "Tras facturar el carrito y el formulario quedan vacíos");
            SoportePruebasRest.exigir(!vista.ocupado, "Al terminar no debe quedar la pantalla ocupada");
        }
    }

    private static final class EjecutorSincrono implements EjecutorTareas {
        @Override public <T> void ejecutar(Tarea<T> tarea, Resultado<T> resultado) {
            try {
                resultado.exito(tarea.ejecutar());
            } catch (IOException ex) {
                resultado.error(ex);
            }
        }

        @Override public void cerrar() { }
    }

    private static final class VistaFalsa implements VistaFacturacion {
        BigDecimal total;
        boolean ocupado;
        boolean limpiado;
        String tituloAviso;
        String mensajeAviso;
        Factura factura;
        List<CuotaAmortizacion> tabla;

        @Override public void setOcupado(boolean ocupado) { this.ocupado = ocupado; }
        @Override public void mostrarAviso(String titulo, String mensaje) { tituloAviso = titulo; mensajeAviso = mensaje; }
        @Override public void setCelulares(List<Celular> celulares) { }
        @Override public void mostrarCarrito(List<CarritoVenta.Linea> lineas, BigDecimal s, BigDecimal d, BigDecimal t) { total = t; }
        @Override public void limpiarFormulario() { limpiado = true; }
        @Override public void mostrarFactura(String mensaje, Factura f, List<CuotaAmortizacion> t) { factura = f; tabla = t; }
    }
}
