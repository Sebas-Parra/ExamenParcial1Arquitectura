package ec.edu.monster.servicios;

import ec.edu.monster.datos.RepositorioComercializadora;
import ec.edu.monster.datos.RepositorioComercializadoraJdbc;
import ec.edu.monster.modelos.Celular;
import ec.edu.monster.modelos.Respuesta;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Objects;

/** Mantenimiento del catálogo de teléfonos celulares a la venta. */
@ApplicationScoped
public class CatalogoServices {
    private final RepositorioComercializadora repositorio;

    public CatalogoServices() {
        this(new RepositorioComercializadoraJdbc());
    }

    public CatalogoServices(RepositorioComercializadora repositorio) {
        this.repositorio = Objects.requireNonNull(repositorio);
    }

    /** Lista los celulares disponibles o, si incluirRetirados es true, todo el catálogo. */
    public Respuesta listarCelulares(boolean incluirRetirados) {
        try {
            Respuesta respuesta = Respuesta.exito("Catalogo de celulares");
            respuesta.setCelulares(repositorio.listarCelulares(!incluirRetirados));
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    public Respuesta obtenerCelular(Integer codCelular) {
        if (codCelular == null || codCelular <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de celular valido");
        try {
            Celular celular = repositorio.buscarCelular(codCelular);
            if (celular == null) return Respuesta.error("CEL-001", "Celular inexistente");
            Respuesta respuesta = Respuesta.exito("Celular encontrado");
            respuesta.setCelular(celular);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    public Respuesta crearCelular(Celular datos) {
        String invalido = validar(datos);
        if (invalido != null) return Respuesta.error("DATA-001", invalido);
        try {
            Celular celular = normalizar(datos, null, Boolean.TRUE);
            celular.setCodCelular(repositorio.crearCelular(celular));
            Respuesta respuesta = Respuesta.exito("Celular registrado");
            respuesta.setCelular(celular);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    public Respuesta actualizarCelular(Integer codCelular, Celular datos) {
        if (codCelular == null || codCelular <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de celular valido");
        String invalido = validar(datos);
        if (invalido != null) return Respuesta.error("DATA-001", invalido);
        try {
            Celular actual = repositorio.buscarCelular(codCelular);
            if (actual == null) return Respuesta.error("CEL-001", "Celular inexistente");
            Celular celular = normalizar(datos, codCelular, actual.getDisponible());
            repositorio.actualizarCelular(celular);
            Respuesta respuesta = Respuesta.exito("Celular actualizado");
            respuesta.setCelular(celular);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Retira el celular del catálogo; las facturas que lo incluyen se conservan. */
    public Respuesta eliminarCelular(Integer codCelular) {
        if (codCelular == null || codCelular <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de celular valido");
        try {
            Celular celular = repositorio.buscarCelular(codCelular);
            if (celular == null) return Respuesta.error("CEL-001", "Celular inexistente");
            repositorio.retirarCelular(codCelular);
            celular.setDisponible(false);
            Respuesta respuesta = Respuesta.exito("Celular retirado del catalogo");
            respuesta.setCelular(celular);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    private static String validar(Celular datos) {
        if (datos == null) return "Indique los datos del celular";
        if (vacio(datos.getMarca()) || datos.getMarca().trim().length() > 50)
            return "La marca es obligatoria y admite hasta 50 caracteres";
        if (vacio(datos.getModelo()) || datos.getModelo().trim().length() > 100)
            return "El modelo es obligatorio y admite hasta 100 caracteres";
        if (datos.getDescripcion() != null && datos.getDescripcion().trim().length() > 255)
            return "La descripcion admite hasta 255 caracteres";
        BigDecimal precio = datos.getPrecio();
        if (precio == null || precio.signum() <= 0) return "El precio debe ser mayor que cero";
        if (precio.stripTrailingZeros().scale() > 2) return "El precio admite como maximo dos decimales";
        if (precio.compareTo(new BigDecimal("99999999.99")) > 0) return "El precio es demasiado alto";
        return null;
    }

    private static Celular normalizar(Celular datos, Integer codCelular, Boolean disponiblePorDefecto) {
        String descripcion = vacio(datos.getDescripcion()) ? null : datos.getDescripcion().trim();
        return new Celular(codCelular, datos.getMarca().trim(), datos.getModelo().trim(), descripcion,
            datos.getPrecio().setScale(2), datos.getDisponible() == null ? disponiblePorDefecto : datos.getDisponible());
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private static Respuesta errorBaseDatos(SQLException e) {
        return Respuesta.error("BD-001", "Error de base de datos: " + e.getMessage());
    }
}
