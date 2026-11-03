package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.RespuestaRest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Lista de facturas (todas o por cédula) y detalle de una factura con su tabla de amortización. */
@WebServlet("/facturas")
public class FacturasServlet extends ControladorBase {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        try {
            if (id != null) {
                detalle(request, response, entero(id));
                return;
            }
            String cedula = texto(request, "cedula");
            request.setAttribute("cedula", cedula);
            if (!cedula.isEmpty() && !cedula.matches("\\d{10}")) {
                error(request, "La cédula debe tener 10 dígitos o quedar vacía.");
            } else {
                RespuestaRest r = api().listarFacturas(cedula);
                if (r.isExito()) request.setAttribute("facturas", r.getFacturas());
                else error(request, mensaje(r));
            }
        } catch (IOException | IllegalArgumentException e) {
            error(request, e.getMessage());
        }
        mostrar(request, response, "facturas", "facturas");
    }

    private void detalle(HttpServletRequest request, HttpServletResponse response, Integer codFactura)
            throws ServletException, IOException {
        if (codFactura == null || codFactura <= 0) {
            error(request, "El número de factura no es válido.");
        } else {
            RespuestaRest r = api().obtenerFactura(codFactura);
            if (r.isExito()) {
                Factura factura = r.getFactura();
                request.setAttribute("factura", factura);
                // Las facturas a crédito se muestran con su tabla de amortización consultada en el banco.
                if (factura.getCodCredito() != null) {
                    RespuestaRest tabla = api().obtenerTablaAmortizacion(factura.getCodCredito());
                    if (tabla.isExito()) request.setAttribute("tabla", tabla.getTablaAmortizacion());
                    else error(request, "No se pudo consultar la tabla de amortización: " + mensaje(tabla));
                }
            } else {
                error(request, mensaje(r));
            }
        }
        mostrar(request, response, "facturas", "factura");
    }
}
