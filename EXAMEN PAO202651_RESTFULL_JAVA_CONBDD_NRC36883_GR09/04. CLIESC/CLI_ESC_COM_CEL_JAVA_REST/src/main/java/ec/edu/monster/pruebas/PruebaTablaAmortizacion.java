package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.modelo.RespuestaRest;
import java.math.BigDecimal;
import java.net.URI;

final class PruebaTablaAmortizacion {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            ClienteRest cliente = new ClienteRest(URI.create(servidor.endpoint()));

            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Creditos del cliente\","
                + "\"nombre\":\"Jorge Esteban Ruiz\",\"creditos\":[{\"codCredito\":1,\"codCliente\":5,\"monto\":600.00,"
                + "\"numeroCuotas\":12,\"tasaAnual\":16.50,\"valorCuota\":54.58,\"fecha\":\"2026-10-12\",\"estado\":\"ACTIVO\"}]}");
            RespuestaRest creditos = cliente.listarCreditos("1756789012");
            SoportePruebasRest.exigir(creditos.isExito() && creditos.getCreditos().size() == 1
                && creditos.getCreditos().get(0).getNumeroCuotas() == 12, "Debe deserializar los creditos");
            SoportePruebasRest.exigir(servidor.ruta.equals("/api/credits?cedula=1756789012"),
                "Los creditos deben consultarse con GET /credits?cedula=");

            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Tabla de amortizacion\","
                + "\"codCredito\":1,\"monto\":600.00,\"numeroCuotas\":12,\"tasaAnual\":16.50,\"tablaAmortizacion\":["
                + "{\"numeroCuota\":1,\"valorCuota\":54.58,\"interesPagado\":8.25,\"capitalPagado\":46.33,\"saldo\":553.67},"
                + "{\"numeroCuota\":12,\"valorCuota\":54.58,\"interesPagado\":0.74,\"capitalPagado\":53.84,\"saldo\":0.00}]}");
            RespuestaRest tabla = cliente.obtenerTablaAmortizacion(1);
            SoportePruebasRest.exigir(tabla.isExito() && tabla.getTablaAmortizacion().size() == 2
                && new BigDecimal("553.67").compareTo(tabla.getTablaAmortizacion().get(0).getSaldo()) == 0,
                "Debe deserializar la tabla de amortizacion");
            SoportePruebasRest.exigir("GET".equals(servidor.metodo) && servidor.ruta.equals("/api/credits/1/amortization"),
                "La tabla debe consultarse con GET /credits/{id}/amortization");

            servidor.responder(404, "{\"estado\":\"ERROR\",\"codigo\":\"CRED-003\",\"mensaje\":\"Credito inexistente\"}");
            SoportePruebasRest.exigir("CRED-003".equals(cliente.obtenerTablaAmortizacion(99).getCodigo()),
                "Debe devolver el error de credito inexistente");
        }
    }
}
