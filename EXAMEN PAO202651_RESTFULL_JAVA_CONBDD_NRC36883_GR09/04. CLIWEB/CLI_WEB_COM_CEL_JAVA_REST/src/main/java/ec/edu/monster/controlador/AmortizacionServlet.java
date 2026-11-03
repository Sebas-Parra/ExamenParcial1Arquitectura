package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Créditos de un cliente en el Banco BanQuito y tabla de amortización del crédito elegido. */
@WebServlet("/amortizacion")
public class AmortizacionServlet extends ControladorBase {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String cedula = texto(request, "cedula");
        Integer credito = entero(request.getParameter("credito"));
        request.setAttribute("cedula", cedula);
        try {
            if (!cedula.isEmpty()) {
                if (!cedula.matches("\\d{10}")) {
                    error(request, "La cédula debe tener 10 dígitos.");
                } else {
                    RespuestaRest creditos = api().listarCreditos(cedula);
                    if (creditos.isExito()) {
                        request.setAttribute("creditos", creditos.getCreditos());
                        request.setAttribute("cliente", creditos.getNombre());
                        if (credito != null) {
                            creditos.getCreditos().stream().filter(c -> c.getCodCredito() == credito).findFirst()
                                .ifPresent(c -> request.setAttribute("creditoSeleccionado", c));
                            RespuestaRest tabla = api().obtenerTablaAmortizacion(credito);
                            if (tabla.isExito()) request.setAttribute("tabla", tabla.getTablaAmortizacion());
                            else error(request, mensaje(tabla));
                        }
                    } else {
                        error(request, mensaje(creditos));
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            error(request, e.getMessage());
        }
        mostrar(request, response, "amortizacion", "amortizacion");
    }
}
