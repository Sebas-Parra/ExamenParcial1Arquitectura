package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.vista.GeneradorPdfFactura;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Descarga una factura real como PDF, sin depender del diálogo de impresión del navegador. */
@WebServlet("/facturas/pdf")
public class FacturaPdfServlet extends ControladorBase {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = entero(request.getParameter("id"));
        if (id == null || id <= 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "El número de factura no es válido.");
            return;
        }

        RespuestaRest resultado = api().obtenerFactura(id);
        if (!resultado.isExito() || resultado.getFactura() == null) {
            int estado = resultado.getEstadoHttp() == HttpServletResponse.SC_NOT_FOUND
                    ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_BAD_GATEWAY;
            response.sendError(estado, mensaje(resultado));
            return;
        }

        Factura factura = resultado.getFactura();
        byte[] pdf = GeneradorPdfFactura.generar(factura);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"factura-" + id + ".pdf\"");
        response.setHeader("Cache-Control", "no-store");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
    }
}
