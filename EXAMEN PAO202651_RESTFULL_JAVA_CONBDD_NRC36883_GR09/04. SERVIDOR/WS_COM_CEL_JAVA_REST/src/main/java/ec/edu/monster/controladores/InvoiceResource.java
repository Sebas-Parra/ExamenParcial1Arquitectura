package ec.edu.monster.controladores;

import ec.edu.monster.modelos.FacturaRequest;
import ec.edu.monster.servicios.FacturacionServices;
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

/** Controlador REST de la facturación (pago en efectivo o crédito directo BanQuito). */
@Path("invoices")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InvoiceResource {
    @Inject
    private FacturacionServices servicio;

    @POST
    public Response facturar(FacturaRequest solicitud) {
        return RestResponseMapper.respuesta(servicio.facturar(solicitud));
    }

    @GET
    public Response listarFacturas(@QueryParam("cedula") String cedula) {
        return RestResponseMapper.respuesta(servicio.listarFacturas(cedula));
    }

    @GET
    @Path("{codFactura: \\d+}")
    public Response obtenerFactura(@PathParam("codFactura") Integer codFactura) {
        return RestResponseMapper.respuesta(servicio.obtenerFactura(codFactura));
    }
}
