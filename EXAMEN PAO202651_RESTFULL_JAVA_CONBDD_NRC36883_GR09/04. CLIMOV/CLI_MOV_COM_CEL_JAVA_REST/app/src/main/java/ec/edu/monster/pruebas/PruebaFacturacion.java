package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.modelo.RespuestaRest;
import java.math.BigDecimal;
import java.util.Collections;

final class PruebaFacturacion {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            ClienteRest cliente = new ClienteRest(servidor.endpoint());

            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Factura registrada con pago en efectivo\","
                + "\"factura\":{\"codFactura\":3,\"formaPago\":\"EFECTIVO\",\"subtotal\":199.00,\"descuento\":83.58,\"total\":115.42,"
                + "\"detalles\":[{\"codCelular\":1,\"marca\":\"Samsung\",\"modelo\":\"Galaxy A15\",\"cantidad\":1,"
                + "\"precioUnitario\":199.00,\"subtotal\":199.00}]}}");
            RespuestaRest efectivo = cliente.facturar(new FacturaRequest("1712345678", "Ana", "EFECTIVO", null,
                Collections.singletonList(new DetalleFactura(1, 1))));
            SoportePruebasRest.exigir(efectivo.isExito() && new BigDecimal("115.42").compareTo(efectivo.getFactura().getTotal()) == 0
                && efectivo.getFactura().getDetalles().size() == 1, "Debe leer la factura en efectivo");
            SoportePruebasRest.exigir("POST".equals(servidor.metodo) && servidor.ruta.equals("/api/invoices")
                && servidor.cuerpo.contains("\"formaPago\":\"EFECTIVO\"") && !servidor.cuerpo.contains("numeroCuotas")
                && servidor.cuerpo.contains("\"codCelular\":1") && servidor.cuerpo.contains("\"cantidad\":1"),
                "Efectivo debe enviar POST /invoices sin numeroCuotas");

            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Factura registrada con credito directo\","
                + "\"factura\":{\"codFactura\":4,\"formaPago\":\"CREDITO\",\"total\":450.00,\"codCredito\":2,\"numeroCuotas\":6,"
                + "\"valorCuota\":78.65},\"codCredito\":2,\"tablaAmortizacion\":["
                + "{\"numeroCuota\":1,\"valorCuota\":78.65,\"interesPagado\":6.19,\"capitalPagado\":72.46,\"saldo\":377.54}]}");
            RespuestaRest credito = cliente.facturar(new FacturaRequest("1712345678", null, "CREDITO", 6,
                Collections.singletonList(new DetalleFactura(4, 1))));
            SoportePruebasRest.exigir(credito.isExito() && credito.getFactura().getCodCredito() == 2
                && credito.getTablaAmortizacion().size() == 1
                && new BigDecimal("6.19").compareTo(credito.getTablaAmortizacion().get(0).getInteresPagado()) == 0,
                "Debe leer el credito y la tabla de amortizacion");
            SoportePruebasRest.exigir(servidor.cuerpo.contains("\"numeroCuotas\":6") && !servidor.cuerpo.contains("nombre"),
                "Credito debe enviar numeroCuotas y omitir el nombre");

            servidor.responder(400, "{\"estado\":\"ERROR\",\"codigo\":\"CRED-002\",\"mensaje\":\"Supera el monto maximo\","
                + "\"montoMaximo\":225.00}");
            RespuestaRest rechazo = cliente.facturar(new FacturaRequest("1767890123", null, "CREDITO", 6,
                Collections.singletonList(new DetalleFactura(2, 1))));
            SoportePruebasRest.exigir(!rechazo.isExito() && "CRED-002".equals(rechazo.getCodigo())
                && new BigDecimal("225.00").compareTo(rechazo.getMontoMaximo()) == 0, "Debe leer el monto maximo del rechazo");
        }
    }
}
