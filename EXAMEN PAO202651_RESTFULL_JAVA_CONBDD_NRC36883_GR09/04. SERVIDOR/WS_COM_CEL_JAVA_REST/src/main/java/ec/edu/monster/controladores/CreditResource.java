package ec.edu.monster.controladores;

import ec.edu.monster.servicios.FacturacionServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Consulta, a través del Banco BanQuito, de los créditos directos y su tabla de amortización. */
@Path("credits")
@Produces(MediaType.APPLICATION_JSON)
public class CreditResource {
    @Inject
    private FacturacionServices servicio;

    @GET
    public Response listarCreditos(@QueryParam("cedula") String cedula) {
        return RestResponseMapper.respuesta(servicio.listarCreditos(cedula));
    }

    @GET
    @Path("{codCredito: \\d+}/amortization")
    public Response obtenerTablaAmortizacion(@PathParam("codCredito") Integer codCredito) {
        return RestResponseMapper.respuesta(servicio.obtenerTablaAmortizacion(codCredito));
    }
}
