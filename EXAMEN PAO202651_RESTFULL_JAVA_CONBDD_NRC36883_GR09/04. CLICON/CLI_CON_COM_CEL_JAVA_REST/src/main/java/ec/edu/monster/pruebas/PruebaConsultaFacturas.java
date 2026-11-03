package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ClienteRest;
import ec.edu.monster.modelo.RespuestaRest;
import java.net.URI;

final class PruebaConsultaFacturas {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Facturas\",\"facturas\":["
                + "{\"codFactura\":2,\"cedula\":\"1756789012\",\"formaPago\":\"CREDITO\",\"total\":600.00,\"codCredito\":1},"
                + "{\"codFactura\":1,\"cedula\":\"1712345678\",\"formaPago\":\"EFECTIVO\",\"total\":115.42}]}");
            ClienteRest cliente = new ClienteRest(URI.create(servidor.endpoint()));
            RespuestaRest todas = cliente.listarFacturas("");
            SoportePruebasRest.exigir(todas.isExito() && todas.getFacturas().size() == 2
                && todas.getFacturas().get(0).getCodCredito() == 1, "Debe deserializar las facturas");
            SoportePruebasRest.exigir("GET".equals(servidor.metodo) && servidor.ruta.equals("/api/invoices"),
                "Sin cedula debe consultar GET /invoices");

            cliente.listarFacturas("1712345678");
            SoportePruebasRest.exigir(servidor.ruta.equals("/api/invoices?cedula=1712345678"),
                "Con cedula debe enviar ?cedula=");
        }
    }
}
