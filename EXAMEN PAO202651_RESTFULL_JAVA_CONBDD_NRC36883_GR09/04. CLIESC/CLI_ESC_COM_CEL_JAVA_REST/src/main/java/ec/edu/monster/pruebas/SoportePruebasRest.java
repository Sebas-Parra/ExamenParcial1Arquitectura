package ec.edu.monster.pruebas;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Servidor HTTP local que simula el API de la comercializadora y registra lo que recibe. */
final class SoportePruebasRest implements AutoCloseable {
    private record Respuesta(int estado, String json) {
    }

    private final HttpServer servidor;
    private final Map<String, Respuesta> respuestas = new LinkedHashMap<>();
    private volatile Respuesta predeterminada =
        new Respuesta(200, "{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"ok\"}");
    final List<String> solicitudes = new ArrayList<>();
    volatile String metodo;
    volatile String ruta;
    volatile String cuerpo;

    SoportePruebasRest() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/", intercambio -> {
            metodo = intercambio.getRequestMethod();
            ruta = intercambio.getRequestURI().toString();
            cuerpo = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            synchronized (solicitudes) {
                solicitudes.add(metodo + " " + ruta);
            }
            Respuesta respuesta = buscar(metodo + " " + ruta);
            byte[] bytes = respuesta.json().getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            intercambio.sendResponseHeaders(respuesta.estado(), bytes.length);
            intercambio.getResponseBody().write(bytes);
            intercambio.close();
        });
        servidor.start();
    }

    String endpoint() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/api/";
    }

    void responder(String json) {
        predeterminada = new Respuesta(200, json);
    }

    void responder(int estado, String json) {
        predeterminada = new Respuesta(estado, json);
    }

    /** Respuesta para las solicitudes cuyo "MÉTODO /api/ruta" comienza con el prefijo indicado. */
    void responder(String prefijo, int estado, String json) {
        synchronized (respuestas) {
            respuestas.put(prefijo, new Respuesta(estado, json));
        }
    }

    private Respuesta buscar(String solicitud) {
        synchronized (respuestas) {
            Respuesta elegida = null;
            int largo = -1;
            for (Map.Entry<String, Respuesta> r : respuestas.entrySet()) {
                if (solicitud.startsWith(r.getKey()) && r.getKey().length() > largo) {
                    elegida = r.getValue();
                    largo = r.getKey().length();
                }
            }
            return elegida == null ? predeterminada : elegida;
        }
    }

    static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new AssertionError(mensaje);
    }

    @Override
    public void close() {
        servidor.stop(0);
    }
}
