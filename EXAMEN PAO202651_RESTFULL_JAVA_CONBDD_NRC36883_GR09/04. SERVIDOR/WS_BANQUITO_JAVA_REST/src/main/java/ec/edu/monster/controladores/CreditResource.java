package ec.edu.monster.controladores;

import ec.edu.monster.modelos.CreditoRequest;
import ec.edu.monster.servicios.CreditoServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Controlador REST del módulo de crédito del Banco BanQuito. */
@Path("credits")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CreditResource {
    @Inject
    private CreditoServices servicio;

    @GET
    @Path("eligibility/{cedula}")
    public Response verificarSujetoCredito(@PathParam("cedula") String cedula) {
        return RestResponseMapper.respuesta(servicio.verificarSujetoCredito(cedula));
    }

    @GET
    @Path("max-amount/{codCliente: \\d+}")
    public Response obtenerMontoMaximo(@PathParam("codCliente") Integer codCliente) {
        return RestResponseMapper.respuesta(servicio.obtenerMontoMaximo(codCliente));
    }

    @POST
    public Response otorgarCredito(CreditoRequest solicitud) {
        return RestResponseMapper.respuesta(servicio.otorgarCredito(
            solicitud == null ? null : solicitud.getCodCliente(),
            solicitud == null ? null : solicitud.getPrecio(),
            solicitud == null ? null : solicitud.getNumeroCuotas()));
    }

    @GET
    @Path("{codCredito: \\d+}/amortization")
    public Response obtenerTablaAmortizacion(@PathParam("codCredito") Integer codCredito) {
        return RestResponseMapper.respuesta(servicio.obtenerTablaAmortizacion(codCredito));
    }

    @GET
    public Response listarCreditos(@QueryParam("cedula") String cedula) {
        return RestResponseMapper.respuesta(servicio.listarCreditos(cedula));
    }
}
