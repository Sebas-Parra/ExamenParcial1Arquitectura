package ec.edu.monster.servicio;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.modelo.RespuestaRest;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/** Adaptador HTTP/JSON para el API REST de la Comercializadora de Teléfonos Celulares. */
public final class ClienteRest {
    private static final Gson GSON = new Gson();
    private static final Duration TIEMPO_RESPUESTA = Duration.ofSeconds(30);

    private final HttpClient http;
    private final URI baseUri;

    public ClienteRest(URI baseUri) {
        this.baseUri = normalizarBase(baseUri);
        this.http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    public RespuestaRest listarCelulares(boolean incluirRetirados) throws IOException {
        return enviar(solicitud(incluirRetirados ? "phones?todos=true" : "phones").GET().build());
    }

    public RespuestaRest crearCelular(Celular celular) throws IOException {
        return enviar(conCuerpo("phones", celular).POST(cuerpo(celular)).build());
    }

    public RespuestaRest actualizarCelular(int codCelular, Celular celular) throws IOException {
        return enviar(conCuerpo("phones/" + codCelular, celular).PUT(cuerpo(celular)).build());
    }

    public RespuestaRest eliminarCelular(int codCelular) throws IOException {
        return enviar(solicitud("phones/" + codCelular).DELETE().build());
    }

    public RespuestaRest facturar(FacturaRequest factura) throws IOException {
        return enviar(conCuerpo("invoices", factura).POST(cuerpo(factura)).build());
    }

    public RespuestaRest listarFacturas(String cedula) throws IOException {
        String ruta = cedula == null || cedula.isBlank() ? "invoices" : "invoices?cedula=" + codificar(cedula.trim());
        return enviar(solicitud(ruta).GET().build());
    }

    public RespuestaRest listarCreditos(String cedula) throws IOException {
        return enviar(solicitud("credits?cedula=" + codificar(Objects.requireNonNull(cedula, "cedula").trim()))
            .GET().build());
    }

    public RespuestaRest obtenerTablaAmortizacion(int codCredito) throws IOException {
        return enviar(solicitud("credits/" + codCredito + "/amortization").GET().build());
    }

    private HttpRequest.Builder solicitud(String ruta) {
        return HttpRequest.newBuilder(baseUri.resolve(ruta))
            .timeout(TIEMPO_RESPUESTA)
            .header("Accept", "application/json");
    }

    private HttpRequest.Builder conCuerpo(String ruta, Object cuerpo) {
        Objects.requireNonNull(cuerpo, "cuerpo");
        return solicitud(ruta).header("Content-Type", "application/json; charset=UTF-8");
    }

    private static HttpRequest.BodyPublisher cuerpo(Object cuerpo) {
        return HttpRequest.BodyPublishers.ofString(GSON.toJson(cuerpo), StandardCharsets.UTF_8);
    }

    private RespuestaRest enviar(HttpRequest request) throws IOException {
        final HttpResponse<String> response;
        try {
            response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("La solicitud al servidor fue interrumpida.", e);
        } catch (IOException e) {
            throw new IOException("No se pudo comunicar con el servidor REST: " + e.getMessage(), e);
        }

        String body = response.body();
        if (body == null || body.isBlank()) {
            throw new IOException("El servidor REST devolvió HTTP " + response.statusCode() + " sin contenido JSON.");
        }
        try {
            RespuestaRest respuesta = GSON.fromJson(body, RespuestaRest.class);
            if (respuesta == null) {
                throw new IOException("El servidor REST devolvió un cuerpo JSON vacío.");
            }
            respuesta.setEstadoHttp(response.statusCode());
            return respuesta;
        } catch (JsonParseException | IllegalStateException e) {
            throw new IOException("El servidor REST devolvió HTTP " + response.statusCode()
                + " con un cuerpo que no es una respuesta JSON válida.", e);
        }
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }

    private static URI normalizarBase(URI uri) {
        Objects.requireNonNull(uri, "baseUri");
        String scheme = uri.getScheme();
        if (!uri.isAbsolute() || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getHost() == null || uri.getRawQuery() != null || uri.getRawFragment() != null) {
            throw new IllegalArgumentException("La URL base debe ser absoluta HTTP/HTTPS y no incluir query ni fragmento.");
        }
        String valor = uri.toString();
        while (valor.endsWith("/")) valor = valor.substring(0, valor.length() - 1);
        return URI.create(valor + "/");
    }
}
