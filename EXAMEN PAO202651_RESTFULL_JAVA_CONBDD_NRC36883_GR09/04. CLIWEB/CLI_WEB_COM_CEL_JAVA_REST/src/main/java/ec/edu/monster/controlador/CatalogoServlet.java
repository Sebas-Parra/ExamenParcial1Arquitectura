package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.RespuestaRest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

/** Mantenimiento del catálogo: listar, registrar, editar y retirar celulares. */
@WebServlet("/catalogo")
public class CatalogoServlet extends ControladorBase {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer editar = entero(request.getParameter("editar"));
        cargar(request, response, editar, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String volver = "/catalogo" + ("true".equals(request.getParameter("todos")) ? "?todos=true" : "");
        Integer codCelular = entero(request.getParameter("codCelular"));
        try {
            if ("retirar".equals(request.getParameter("accion"))) {
                if (codCelular == null) {
                    error(request, "Selecciona el celular que deseas retirar.");
                } else {
                    RespuestaRest r = api().eliminarCelular(codCelular);
                    if (r.isExito()) {
                        redirigir(request, response, volver, r.getCelular().nombre() + " se retiró del catálogo.");
                        return;
                    }
                    error(request, mensaje(r));
                }
                cargar(request, response, null, null);
                return;
            }

            Celular datos = leerFormulario(request, codCelular != null);
            if (request.getAttribute("error") == null) {
                RespuestaRest r = codCelular == null ? api().crearCelular(datos) : api().actualizarCelular(codCelular, datos);
                if (r.isExito()) {
                    redirigir(request, response, volver, codCelular == null
                        ? r.getCelular().nombre() + " se registró con el código " + r.getCelular().getCodCelular() + "."
                        : r.getCelular().nombre() + " se actualizó.");
                    return;
                }
                error(request, mensaje(r));
            }
            datos.setCodCelular(codCelular);
            cargar(request, response, codCelular, datos);
        } catch (IOException | IllegalArgumentException e) {
            error(request, e.getMessage());
            cargar(request, response, null, null);
        }
    }

    /** Lee el formulario; si hay datos inválidos deja el error en la petición. */
    private Celular leerFormulario(HttpServletRequest request, boolean edicion) {
        String marca = texto(request, "marca");
        String modelo = texto(request, "modelo");
        String descripcion = texto(request, "descripcion");
        String precioTexto = texto(request, "precio").replace(',', '.');
        BigDecimal precio = null;
        if (marca.isEmpty()) error(request, "Escribe la marca.");
        else if (modelo.isEmpty()) error(request, "Escribe el modelo.");
        try {
            precio = new BigDecimal(precioTexto);
            if (precio.signum() <= 0 || precio.stripTrailingZeros().scale() > 2)
                error(request, "El precio debe ser mayor que cero y tener hasta dos decimales.");
        } catch (NumberFormatException e) {
            error(request, "Escribe un precio válido; puedes usar punto o coma decimal.");
        }
        Celular celular = new Celular(marca, modelo, descripcion.isEmpty() ? null : descripcion, precio);
        if (edicion) celular.setDisponible(request.getParameter("disponible") != null);
        request.setAttribute("precioIngresado", texto(request, "precio"));
        return celular;
    }

    /** Muestra el catálogo; formulario es el celular en edición o los datos rechazados del formulario. */
    private void cargar(HttpServletRequest request, HttpServletResponse response, Integer editar, Celular formulario)
            throws ServletException, IOException {
        boolean todos = "true".equals(request.getParameter("todos"));
        request.setAttribute("todos", todos);
        try {
            RespuestaRest r = api().listarCelulares(todos || editar != null);
            if (r.isExito()) {
                request.setAttribute("celulares", r.getCelulares().stream()
                    .filter(c -> todos || !Boolean.FALSE.equals(c.getDisponible())).toList());
                if (formulario == null && editar != null) {
                    formulario = r.getCelulares().stream().filter(c -> editar.equals(c.getCodCelular())).findFirst().orElse(null);
                    if (formulario == null) error(request, "El celular " + editar + " no existe.");
                }
            } else {
                error(request, mensaje(r));
            }
        } catch (IOException | IllegalArgumentException e) {
            error(request, e.getMessage());
        }
        request.setAttribute("formulario", formulario);
        mostrar(request, response, "catalogo", "catalogo");
    }
}
