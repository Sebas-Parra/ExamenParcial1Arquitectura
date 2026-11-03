package ec.edu.monster.pruebas;

import ec.edu.monster.cliente.ClienteRest;
import ec.edu.monster.consola.controlador.ControladorConsola;
import ec.edu.monster.consola.vista.VistaConsola;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

/** Recorre los menús de consola con entradas guionizadas contra el servidor simulado. */
final class PruebaFlujoConsola {
    private static final String CATALOGO = "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Catalogo\",\"celulares\":["
        + "{\"codCelular\":1,\"marca\":\"Samsung\",\"modelo\":\"Galaxy A15\",\"precio\":199.00,\"disponible\":true},"
        + "{\"codCelular\":4,\"marca\":\"Samsung\",\"modelo\":\"Galaxy A55\",\"precio\":450.00,\"disponible\":true}]}";

    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("GET /api/phones", 200, CATALOGO);
            servidor.responder("POST /api/invoices", 200, "{\"estado\":\"EXITO\",\"codigo\":\"OK\","
                + "\"mensaje\":\"Factura registrada con credito directo\",\"factura\":{\"codFactura\":4,"
                + "\"cedula\":\"1712345678\",\"nombre\":\"Ana Lucía Torres\",\"fecha\":\"2026-11-01T10:00:00\","
                + "\"formaPago\":\"CREDITO\",\"subtotal\":450.00,\"descuento\":0.00,\"total\":450.00,\"codCredito\":2,"
                + "\"numeroCuotas\":6,\"valorCuota\":78.65,\"detalles\":[{\"codCelular\":4,\"marca\":\"Samsung\","
                + "\"modelo\":\"Galaxy A55\",\"cantidad\":1,\"precioUnitario\":450.00,\"subtotal\":450.00}]},"
                + "\"codCredito\":2,\"tablaAmortizacion\":[{\"numeroCuota\":1,\"valorCuota\":78.65,"
                + "\"interesPagado\":6.19,\"capitalPagado\":72.46,\"saldo\":377.54}]}");
            servidor.responder("GET /api/credits?", 200, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Creditos\","
                + "\"creditos\":[{\"codCredito\":2,\"monto\":450.00,\"numeroCuotas\":6,\"valorCuota\":78.65,"
                + "\"fecha\":\"2026-11-01\",\"estado\":\"ACTIVO\"}]}");
            servidor.responder("GET /api/credits/2/amortization", 200, "{\"estado\":\"EXITO\",\"codigo\":\"OK\","
                + "\"mensaje\":\"Tabla\",\"codCredito\":2,\"monto\":450.00,\"numeroCuotas\":6,\"tasaAnual\":16.50,"
                + "\"tablaAmortizacion\":[{\"numeroCuota\":6,\"valorCuota\":78.65,\"interesPagado\":1.07,"
                + "\"capitalPagado\":77.58,\"saldo\":0.00}]}");

            String entradas = String.join("\n",
                "5", "2", "1", "n", "2", "1712345678", "6", "s",      // venta con crédito directo
                "5", "1", "2", "n", "1", "1712345678", "Ana", "n",   // venta en efectivo cancelada
                "7", "1712345678", "1",                              // tabla de amortización
                "0") + "\n";
            StringWriter texto = new StringWriter();
            VistaConsola vista = new VistaConsola(new BufferedReader(new StringReader(entradas)), new PrintWriter(texto, true));
            new ControladorConsola(new ClienteRest(URI.create(servidor.endpoint())), vista).ejecutar();
            String salida = texto.toString();

            SoportePruebasRest.exigir(salida.contains("FACTURA N.º 4") && salida.contains("Crédito BanQuito N.º 2: 6 cuotas"),
                "La venta a crédito debe mostrar la factura y el crédito");
            SoportePruebasRest.exigir(salida.contains("TABLA DE AMORTIZACIÓN") && salida.contains("$377.54"),
                "La venta a crédito debe mostrar la tabla de amortización");
            SoportePruebasRest.exigir(salida.contains("Descuento 42%: $167.16") && salida.contains("Total a pagar: $230.84")
                && salida.contains("Venta cancelada."), "La venta en efectivo debe mostrar el descuento antes de confirmar");
            SoportePruebasRest.exigir(salida.contains("TABLA DE AMORTIZACIÓN | Crédito N.º 2") && salida.contains("$1.07"),
                "La consulta debe mostrar la tabla del crédito elegido");
            long facturas = servidor.solicitudes.stream().filter(s -> s.startsWith("POST /api/invoices")).count();
            SoportePruebasRest.exigir(facturas == 1, "La venta cancelada no debe enviarse al servidor");
            SoportePruebasRest.exigir(salida.contains("Hasta luego."), "Debe salir con la opción 0");
        }
    }
}
