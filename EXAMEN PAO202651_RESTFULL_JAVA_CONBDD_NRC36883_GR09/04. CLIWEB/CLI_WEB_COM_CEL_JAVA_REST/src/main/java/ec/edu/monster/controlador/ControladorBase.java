package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.ComercializadoraWebService;
import ec.edu.monster.vista.VistaHtml;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Utilidades comunes de los servlets: cliente REST, mensajes de error, avisos tras redirigir y vistas. */
abstract class ControladorBase extends HttpServlet {
    private static final String AVISO = "aviso";

    protected ClienteRest api() {
        return ComercializadoraWebService.cliente(getServletContext());
    }

    /** Muestra la vista JSP indicada, pasando el aviso pendiente de la redirección anterior. */
    protected void mostrar(HttpServletRequest request, HttpServletResponse response, String seccion, String vista)
            throws ServletException, IOException {
        var sesion = request.getSession(false);
        if (sesion != null && sesion.getAttribute(AVISO) != null) {
            request.setAttribute("aviso", sesion.getAttribute(AVISO));
            sesion.removeAttribute(AVISO);
        }
        request.setAttribute("seccion", seccion);
        request.getRequestDispatcher("/WEB-INF/vista/" + vista + ".jsp").forward(request, response);
    }

    /** Redirige (patrón POST-redirect-GET) dejando un aviso de éxito para la siguiente página. */
    protected void redirigir(HttpServletRequest request, HttpServletResponse response, String ruta, String aviso)
            throws IOException {
        if (aviso != null) request.getSession().setAttribute(AVISO, aviso);
        response.sendRedirect(request.getContextPath() + ruta);
    }

    /** Guarda el primer error de la petición para mostrarlo en la vista. */
    protected void error(HttpServletRequest request, String mensaje) {
        if (request.getAttribute("error") == null) request.setAttribute("error", mensaje);
    }

    /** Texto de un error del servidor, con el motivo del banco o el monto máximo cuando vienen. */
    protected static String mensaje(RespuestaRest r) {
        StringBuilder texto = new StringBuilder(r.getMensaje() == null || r.getMensaje().isBlank()
            ? "El servidor devolvió una respuesta incompleta." : r.getMensaje());
        if (r.getMotivo() != null) texto.append(". Motivo: ").append(r.getMotivo());
        if (r.getMontoMaximo() != null)
            texto.append(". Monto máximo de crédito aprobado: ").append(VistaHtml.dinero(r.getMontoMaximo()));
        if (r.getCodigo() != null) texto.append(" (").append(r.getCodigo()).append(")");
        return texto.toString();
    }

    protected static Integer entero(String texto) {
        try {
            return texto == null || texto.isBlank() ? null : Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static String texto(HttpServletRequest request, String parametro) {
        String valor = request.getParameter(parametro);
        return valor == null ? "" : valor.trim();
    }
}
