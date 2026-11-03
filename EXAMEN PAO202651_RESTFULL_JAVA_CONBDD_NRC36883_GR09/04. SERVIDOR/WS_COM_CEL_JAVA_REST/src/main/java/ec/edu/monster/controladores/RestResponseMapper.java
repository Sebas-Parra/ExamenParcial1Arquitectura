package ec.edu.monster.controladores;

import ec.edu.monster.modelos.Respuesta;
import jakarta.ws.rs.core.Response;
import java.util.Set;

/** Adapta las respuestas del modelo de negocio al protocolo HTTP. */
public final class RestResponseMapper {
    private static final Set<String> NO_ENCONTRADO = Set.of("CEL-001", "FAC-001", "CLI-001", "CRED-003");

    private RestResponseMapper() {
    }

    public static Response respuesta(Respuesta respuesta) {
        String codigo = respuesta.getCodigo();
        Response.Status estadoHttp = "OK".equals(codigo) ? Response.Status.OK
            : NO_ENCONTRADO.contains(codigo) ? Response.Status.NOT_FOUND
            : codigo != null && codigo.startsWith("BANCO-") ? Response.Status.BAD_GATEWAY
            : codigo != null && codigo.startsWith("BD-") ? Response.Status.INTERNAL_SERVER_ERROR
            : Response.Status.BAD_REQUEST;
        return Response.status(estadoHttp).entity(respuesta).build();
    }
}
