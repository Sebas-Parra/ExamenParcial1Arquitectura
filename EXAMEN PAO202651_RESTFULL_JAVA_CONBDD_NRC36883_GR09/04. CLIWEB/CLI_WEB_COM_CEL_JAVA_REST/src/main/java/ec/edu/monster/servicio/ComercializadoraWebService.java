package ec.edu.monster.servicio;

import jakarta.servlet.ServletContext;
import java.net.URI;

/** Crea el cliente REST de la comercializadora con el endpoint configurado en web.xml o como propiedad JVM. */
public final class ComercializadoraWebService {
    private ComercializadoraWebService() { }

    public static ClienteRest cliente(ServletContext contexto) {
        String url = contexto.getInitParameter("rest.endpoint");
        if (url == null || url.isBlank()) {
            url = System.getProperty("comercializadora.rest.endpoint");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("Configura rest.endpoint para conectar con el servidor REST.");
        }
        try {
            return new ClienteRest(URI.create(url.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("El endpoint REST configurado no es válido.", e);
        }
    }
}
