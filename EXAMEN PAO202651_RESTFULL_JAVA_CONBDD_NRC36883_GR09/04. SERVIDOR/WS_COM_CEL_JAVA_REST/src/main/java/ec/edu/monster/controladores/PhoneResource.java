package ec.edu.monster.controladores;

import ec.edu.monster.modelos.Celular;
import ec.edu.monster.servicios.CatalogoServices;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/** Controlador REST del mantenimiento del catálogo de teléfonos celulares. */
@Path("phones")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PhoneResource {
    @Inject
    private CatalogoServices servicio;

    @GET
    public Response listarCelulares(@QueryParam("todos") boolean todos) {
        return RestResponseMapper.respuesta(servicio.listarCelulares(todos));
    }

    @GET
    @Path("{codCelular: \\d+}")
    public Response obtenerCelular(@PathParam("codCelular") Integer codCelular) {
        return RestResponseMapper.respuesta(servicio.obtenerCelular(codCelular));
    }

    @POST
    public Response crearCelular(Celular celular) {
        return RestResponseMapper.respuesta(servicio.crearCelular(celular));
    }

    @PUT
    @Path("{codCelular: \\d+}")
    public Response actualizarCelular(@PathParam("codCelular") Integer codCelular, Celular celular) {
        return RestResponseMapper.respuesta(servicio.actualizarCelular(codCelular, celular));
    }

    @DELETE
    @Path("{codCelular: \\d+}")
    public Response eliminarCelular(@PathParam("codCelular") Integer codCelular) {
        return RestResponseMapper.respuesta(servicio.eliminarCelular(codCelular));
    }
}
