package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ClienteRest;
import java.math.BigDecimal;

final class PruebaDetalleFactura {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            ClienteRest cliente = new ClienteRest(servidor.endpoint());
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Factura encontrada\",\"factura\":"
                + "{\"codFactura\":2,\"cedula\":\"1756789012\",\"formaPago\":\"CREDITO\",\"total\":600.00,\"codCredito\":1,"
                + "\"numeroCuotas\":12,\"detalles\":[{\"codCelular\":5,\"marca\":\"Apple\",\"modelo\":\"iPhone 13\","
                + "\"cantidad\":1,\"precioUnitario\":600.00,\"subtotal\":600.00}]}}");
            RespuestaRest respuesta = cliente.obtenerFactura(2);
            SoportePruebasRest.exigir(respuesta.isExito() && respuesta.getFactura().getCodCredito() == 1
                && new BigDecimal("600.00").compareTo(respuesta.getFactura().getDetalles().get(0).getSubtotal()) == 0,
                "Debe deserializar la factura con su detalle");
            SoportePruebasRest.exigir("GET".equals(servidor.metodo) && servidor.ruta.equals("/api/invoices/2"),
                "La factura debe consultarse con GET /invoices/{id}");

            servidor.responder(404, "{\"estado\":\"ERROR\",\"codigo\":\"FAC-001\",\"mensaje\":\"Factura inexistente\"}");
            SoportePruebasRest.exigir("FAC-001".equals(cliente.obtenerFactura(999).getCodigo()),
                "Debe devolver el error de factura inexistente");
        }
    }
}
