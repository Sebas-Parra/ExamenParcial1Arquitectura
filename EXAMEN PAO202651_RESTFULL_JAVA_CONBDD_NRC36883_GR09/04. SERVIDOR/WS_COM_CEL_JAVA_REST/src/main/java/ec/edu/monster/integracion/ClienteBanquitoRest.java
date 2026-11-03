package ec.edu.monster.integracion;

import ec.edu.monster.modelos.Respuesta;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Cliente HTTP del API REST del Banco BanQuito (WS_BANQUITO_JAVA_REST). */
public class ClienteBanquitoRest implements ServicioBanco {
    private static final Duration TIEMPO_ESPERA = Duration.ofSeconds(15);

    private final String urlBase;
    private final HttpClient http;
    private final Jsonb jsonb;

    public ClienteBanquitoRest() {
        this(System.getProperty("comercializadora.banco.url", "http://localhost:8080/WS_BANQUITO_JAVA_REST/api"));
    }

    public ClienteBanquitoRest(String urlBase) {
        this.urlBase = urlBase.endsWith("/") ? urlBase.substring(0, urlBase.length() - 1) : urlBase;
        this.http = HttpClient.newBuilder().connectTimeout(TIEMPO_ESPERA).build();
        this.jsonb = JsonbBuilder.create();
    }

    @Override
    public Respuesta verificarSujetoCredito(String cedula) {
        return enviar(get("/credits/eligibility/" + codificar(cedula)));
    }

    @Override
    public Respuesta obtenerMontoMaximo(int codCliente) {
        return enviar(get("/credits/max-amount/" + codCliente));
    }

    @Override
    public Respuesta otorgarCredito(int codCliente, BigDecimal precio, int numeroCuotas) {
        String json = "{\"codCliente\":" + codCliente + ",\"precio\":" + precio.toPlainString()
            + ",\"numeroCuotas\":" + numeroCuotas + "}";
        return enviar(HttpRequest.newBuilder(URI.create(urlBase + "/credits")).timeout(TIEMPO_ESPERA)
            .header("Content-Type", "application/json").header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)).build());
    }

    @Override
    public Respuesta obtenerTablaAmortizacion(int codCredito) {
        return enviar(get("/credits/" + codCredito + "/amortization"));
    }

    @Override
    public Respuesta listarCreditos(String cedula) {
        return enviar(get("/credits?cedula=" + codificar(cedula)));
    }

    private HttpRequest get(String ruta) {
        return HttpRequest.newBuilder(URI.create(urlBase + ruta)).timeout(TIEMPO_ESPERA)
            .header("Accept", "application/json").GET().build();
    }

    /** El banco responde JSON con estado, codigo y mensaje también en los errores 4xx y 5xx. */
    private Respuesta enviar(HttpRequest solicitud) {
        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(solicitud, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new BancoNoDisponibleException("No se pudo conectar con el Banco BanQuito en " + urlBase, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BancoNoDisponibleException("Se interrumpió la llamada al Banco BanQuito", e);
        }
        try {
            Respuesta cuerpo = jsonb.fromJson(respuesta.body(), Respuesta.class);
            if (cuerpo == null || cuerpo.getCodigo() == null)
                throw new IllegalStateException("respuesta sin codigo");
            return cuerpo;
        } catch (RuntimeException e) {
            throw new BancoNoDisponibleException("Respuesta no válida del Banco BanQuito (HTTP "
                + respuesta.statusCode() + ")", e);
        }
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
