package ec.edu.monster.controlador;

import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.modelo.RespuestaRest;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/** Venta de celulares: carrito en la sesión y facturación en efectivo o con crédito directo BanQuito. */
@WebServlet("/facturar")
public class FacturacionServlet extends ControladorBase {
    private static final String CARRITO = "carrito";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        cargar(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        CarritoVenta carrito = carrito(request);
        String accion = texto(request, "accion");
        try {
            switch (accion) {
                case "agregar" -> {
                    Integer codCelular = entero(request.getParameter("codCelular"));
                    Integer cantidad = entero(request.getParameter("cantidad"));
                    Celular celular = codCelular == null ? null : buscarDisponible(codCelular);
                    if (celular == null) error(request, "Selecciona un celular disponible.");
                    else if (cantidad == null || cantidad < 1) error(request, "La cantidad debe ser mayor que cero.");
                    else {
                        carrito.agregar(celular, cantidad);
                        redirigir(request, response, "/facturar", null);
                        return;
                    }
                }
                case "quitar" -> {
                    Integer indice = entero(request.getParameter("indice"));
                    if (indice != null && indice >= 0 && indice < carrito.lineas().size()) carrito.quitar(indice);
                    redirigir(request, response, "/facturar", null);
                    return;
                }
                case "vaciar" -> {
                    carrito.vaciar();
                    redirigir(request, response, "/facturar", null);
                    return;
                }
                case "facturar" -> {
                    if (facturar(request, response, carrito)) return;
                }
                default -> error(request, "Acción no reconocida.");
            }
        } catch (IOException | IllegalArgumentException e) {
            error(request, e.getMessage());
        }
        cargar(request, response);
    }

    /** Devuelve true si la factura se registró y ya se redirigió al detalle. */
    private boolean facturar(HttpServletRequest request, HttpServletResponse response, CarritoVenta carrito)
            throws IOException {
        String formaPago = CarritoVenta.CREDITO.equals(texto(request, "formaPago")) ? CarritoVenta.CREDITO : CarritoVenta.EFECTIVO;
        String cedula = texto(request, "cedula");
        String nombre = texto(request, "nombre");
        Integer cuotas = entero(request.getParameter("cuotas"));
        request.setAttribute("formaPago", formaPago);
        request.setAttribute("cedula", cedula);
        request.setAttribute("nombre", nombre);
        request.setAttribute("cuotas", cuotas);

        boolean enEfectivo = CarritoVenta.EFECTIVO.equals(formaPago);
        if (carrito.estaVacio()) error(request, "Agrega al menos un celular al carrito.");
        else if (!cedula.matches("\\d{10}")) error(request, "La cédula debe tener 10 dígitos.");
        else if (enEfectivo && nombre.isEmpty()) error(request, "Escribe el nombre del comprador.");
        else if (!enEfectivo && (cuotas == null || cuotas < 3 || cuotas > 18))
            error(request, "El número de cuotas debe estar entre 3 y 18.");
        if (request.getAttribute("error") != null) return false;

        RespuestaRest r = api().facturar(new FacturaRequest(cedula, enEfectivo ? nombre : null, formaPago,
            enEfectivo ? null : cuotas, carrito.detalles()));
        if (!r.isExito()) {
            error(request, mensaje(r));
            return false;
        }
        carrito.vaciar();
        redirigir(request, response, "/facturas?id=" + r.getFactura().getCodFactura(), r.getMensaje());
        return true;
    }

    private Celular buscarDisponible(int codCelular) throws IOException {
        RespuestaRest r = api().listarCelulares(false);
        if (!r.isExito()) throw new IOException(mensaje(r));
        return r.getCelulares().stream().filter(c -> c.getCodCelular() == codCelular).findFirst().orElse(null);
    }

    private void cargar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("carritoVenta", carrito(request));
        try {
            RespuestaRest r = api().listarCelulares(false);
            if (r.isExito()) request.setAttribute("celulares", r.getCelulares());
            else error(request, mensaje(r));
        } catch (IOException | IllegalArgumentException e) {
            error(request, e.getMessage());
        }
        if (request.getAttribute("celulares") == null) request.setAttribute("celulares", List.<Celular>of());
        mostrar(request, response, "facturar", "facturar");
    }

    private static CarritoVenta carrito(HttpServletRequest request) {
        var sesion = request.getSession();
        CarritoVenta carrito = (CarritoVenta) sesion.getAttribute(CARRITO);
        if (carrito == null) {
            carrito = new CarritoVenta();
            sesion.setAttribute(CARRITO, carrito);
        }
        return carrito;
    }
}
