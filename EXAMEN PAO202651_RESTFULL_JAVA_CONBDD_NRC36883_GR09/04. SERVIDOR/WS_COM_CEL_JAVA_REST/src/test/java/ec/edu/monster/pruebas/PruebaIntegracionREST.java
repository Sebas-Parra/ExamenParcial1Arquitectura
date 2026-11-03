package ec.edu.monster.pruebas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prueba HTTP contra los dos WAR desplegados (comercializadora y banco) con COMERCIALIZADORA.sql
 * y BANQUITO.sql recién cargados. No otorga créditos, así que puede repetirse sin recargar el banco;
 * cada ejecución deja un celular de prueba retirado y una factura en efectivo.
 */
public class PruebaIntegracionREST {
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();
    private static int realizadas;

    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "http://localhost:8080/WS_COM_CEL_JAVA_REST/api";

        comprobar(get(base + "/phones"), 200, "Galaxy A15", "catálogo de celulares");
        comprobar(get(base + "/phones/999999"), 404, "CEL-001", "celular inexistente");
        comprobar(post(base + "/phones", "{\"marca\":\"Prueba\",\"precio\":-1}"), 400, "DATA-001", "celular inválido");

        HttpResponse<String> creado = post(base + "/phones",
            "{\"marca\":\"Prueba\",\"modelo\":\"Integracion\",\"precio\":99.99}");
        comprobar(creado, 200, "\"disponible\":true", "crear celular");
        String codCelular = extraer(creado.body(), "\"codCelular\":(\\d+)");
        comprobar(put(base + "/phones/" + codCelular, "{\"marca\":\"Prueba\",\"modelo\":\"Integracion\",\"precio\":89.5}"),
            200, "\"precio\":89.50", "actualizar celular");
        comprobar(delete(base + "/phones/" + codCelular), 200, "\"disponible\":false", "retirar celular");
        comprobar(post(base + "/invoices", factura("1712345678", "EFECTIVO", "null", codCelular)),
            400, "CEL-002", "facturar un celular retirado");

        comprobar(post(base + "/invoices", "{\"cedula\":\"1712345678\",\"nombre\":\"Ana Lucía Torres\","
            + "\"formaPago\":\"EFECTIVO\",\"detalles\":[{\"codCelular\":1,\"cantidad\":1}]}"),
            200, "\"total\":115.42", "venta en efectivo con 42% de descuento");
        comprobar(post(base + "/invoices", factura("1734567890", "CREDITO", "6", "1")),
            400, "CRED-001", "crédito a un hombre menor de 25 años");
        comprobar(post(base + "/invoices", factura("1756789012", "CREDITO", "6", "1")),
            400, "CRED-001", "crédito a quien ya tiene uno activo");
        comprobar(post(base + "/invoices", factura("1767890123", "CREDITO", "6", "2")),
            400, "CRED-002", "compra mayor al monto máximo de Sofía");
        comprobar(post(base + "/invoices", factura("1712345678", "CREDITO", "24", "1")),
            400, "DATA-001", "plazo fuera de rango");

        comprobar(get(base + "/invoices/2"), 200, "\"codCredito\":1", "factura a crédito de Jorge");
        comprobar(get(base + "/invoices/999999"), 404, "FAC-001", "factura inexistente");
        comprobar(get(base + "/invoices?cedula=1712345678"), 200, "\"formaPago\":\"EFECTIVO\"", "facturas de Ana");

        comprobar(get(base + "/credits?cedula=1756789012"), 200, "\"codCredito\":1", "créditos de Jorge en el banco");
        comprobar(get(base + "/credits/1/amortization"), 200, "\"numeroCuota\":12", "tabla de amortización de Jorge");
        comprobar(get(base + "/credits/9999/amortization"), 404, "CRED-003", "crédito inexistente");

        System.out.println("Prueba de integracion REST: " + realizadas + " verificaciones OK en " + base);
    }

    private static String factura(String cedula, String formaPago, String cuotas, String codCelular) {
        return "{\"cedula\":\"" + cedula + "\",\"nombre\":\"Comprador\",\"formaPago\":\"" + formaPago
            + "\",\"numeroCuotas\":" + cuotas + ",\"detalles\":[{\"codCelular\":" + codCelular + ",\"cantidad\":1}]}";
    }

    private static String extraer(String texto, String patron) {
        Matcher m = Pattern.compile(patron).matcher(texto.replace(" ", ""));
        if (!m.find()) throw new AssertionError("No se encontró " + patron + " en " + texto);
        return m.group(1);
    }

    private static HttpResponse<String> get(String url) throws Exception {
        return enviar(HttpRequest.newBuilder(URI.create(url)).GET());
    }

    private static HttpResponse<String> delete(String url) throws Exception {
        return enviar(HttpRequest.newBuilder(URI.create(url)).DELETE());
    }

    private static HttpResponse<String> post(String url, String json) throws Exception {
        return enviar(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)));
    }

    private static HttpResponse<String> put(String url, String json) throws Exception {
        return enviar(HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/json")
            .PUT(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)));
    }

    private static HttpResponse<String> enviar(HttpRequest.Builder solicitud) throws Exception {
        return CLIENTE.send(solicitud.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static void comprobar(HttpResponse<String> respuesta, int estado, String contiene, String descripcion) {
        realizadas++;
        String cuerpo = respuesta.body().replace(" ", "");
        if (respuesta.statusCode() != estado || !cuerpo.contains(contiene.replace(" ", "")))
            throw new AssertionError(descripcion + ": se esperaba HTTP " + estado + " con " + contiene
                + " y se recibió HTTP " + respuesta.statusCode() + " " + respuesta.body());
        System.out.println("OK  " + descripcion);
    }
}
