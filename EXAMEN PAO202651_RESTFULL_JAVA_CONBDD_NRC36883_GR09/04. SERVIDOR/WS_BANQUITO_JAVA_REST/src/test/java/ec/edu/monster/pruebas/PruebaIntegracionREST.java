package ec.edu.monster.pruebas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * Prueba HTTP contra el WAR desplegado y la base cargada con BANQUITO.sql recién ejecutado.
 * Solo consulta datos: no otorga créditos para que pueda repetirse sin volver a cargar la base.
 */
public class PruebaIntegracionREST {
    private static final HttpClient CLIENTE = HttpClient.newHttpClient();
    private static int realizadas;

    public static void main(String[] args) throws Exception {
        String base = args.length > 0 ? args[0] : "http://localhost:8080/WS_BANQUITO_JAVA_REST/api";

        comprobar(get(base + "/credits/eligibility/1712345678"), 200, "\"sujetoCredito\":true", "Ana es sujeto de crédito");
        comprobar(get(base + "/credits/eligibility/1734567890"), 200, "\"sujetoCredito\":false", "Luis es menor de 25 años");
        comprobar(get(base + "/credits/eligibility/1745678901"), 200, "\"sujetoCredito\":false", "María no depositó en el último mes");
        comprobar(get(base + "/credits/eligibility/1756789012"), 200, "\"sujetoCredito\":false", "Jorge tiene crédito activo");
        comprobar(get(base + "/credits/eligibility/9999999999"), 200, "no es cliente", "persona que no es cliente");

        comprobar(get(base + "/credits/max-amount/6"), 200, "\"montoMaximo\":225", "monto máximo de Sofía");
        comprobar(get(base + "/credits/max-amount/999"), 404, "CLI-001", "monto máximo de cliente inexistente");

        comprobar(post(base + "/credits", "{\"codCliente\":6,\"precio\":500,\"numeroCuotas\":6}"),
            400, "CRED-002", "precio mayor al monto máximo");
        comprobar(post(base + "/credits", "{\"codCliente\":3,\"precio\":100,\"numeroCuotas\":6}"),
            400, "CRED-001", "cliente que no es sujeto de crédito");
        comprobar(post(base + "/credits", "{\"codCliente\":1,\"precio\":100,\"numeroCuotas\":24}"),
            400, "DATA-001", "plazo fuera de rango");

        comprobar(get(base + "/credits/1/amortization"), 200, "\"numeroCuota\":12", "tabla del crédito de Jorge");
        comprobar(get(base + "/credits/9999/amortization"), 404, "CRED-003", "crédito inexistente");
        comprobar(get(base + "/credits?cedula=1756789012"), 200, "\"codCredito\":1", "créditos de Jorge");

        System.out.println("Prueba de integracion REST: " + realizadas + " verificaciones OK en " + base);
    }

    private static HttpResponse<String> get(String url) throws Exception {
        return CLIENTE.send(HttpRequest.newBuilder(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static HttpResponse<String> post(String url, String json) throws Exception {
        return CLIENTE.send(HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build(),
            HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
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
