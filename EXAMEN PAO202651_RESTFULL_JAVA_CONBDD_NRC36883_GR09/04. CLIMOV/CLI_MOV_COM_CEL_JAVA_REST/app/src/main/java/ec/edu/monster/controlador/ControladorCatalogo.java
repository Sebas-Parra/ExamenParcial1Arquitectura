package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Celular;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.VistaCatalogo;
import java.math.BigDecimal;

/** Mantenimiento del catálogo de celulares. */
public final class ControladorCatalogo {
    private final ClienteRest cliente;
    private final EjecutorTareas ejecutor;
    private final VistaCatalogo vista;

    public ControladorCatalogo(ClienteRest cliente, EjecutorTareas ejecutor, VistaCatalogo vista) {
        this.cliente = cliente;
        this.ejecutor = ejecutor;
        this.vista = vista;
    }

    public void cargar() {
        boolean todos = vista.incluirRetirados();
        LlamadaRest.ejecutar(ejecutor, vista, "Catálogo", () -> cliente.listarCelulares(todos),
            r -> vista.mostrarCelulares(r.getCelulares()));
    }

    /** Valida los datos del formulario; devuelve el error o null si son correctos. */
    public static String validar(String marca, String modelo, String precio) {
        if (marca == null || marca.trim().isEmpty()) return "Escribe la marca.";
        if (modelo == null || modelo.trim().isEmpty()) return "Escribe el modelo.";
        try {
            BigDecimal valor = new BigDecimal(precio.trim().replace(',', '.'));
            if (valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2)
                return "El precio debe ser mayor que cero y tener hasta dos decimales.";
        } catch (RuntimeException ex) {
            return "Escribe un precio válido; puedes usar punto o coma decimal.";
        }
        return null;
    }

    /** Registra el celular (codCelular null) o actualiza uno existente. */
    public void guardar(Integer codCelular, Celular datos) {
        LlamadaRest.ejecutar(ejecutor, vista, codCelular == null ? "No se pudo registrar" : "No se pudo actualizar",
            () -> codCelular == null ? cliente.crearCelular(datos) : cliente.actualizarCelular(codCelular, datos), r -> {
                vista.mostrarAviso(codCelular == null ? "Celular registrado" : "Celular actualizado",
                    r.getCelular().nombre() + (codCelular == null
                        ? " se registró con el código " + r.getCelular().getCodCelular() + "." : " se actualizó."));
                cargar();
            });
    }

    public void retirar(Celular celular) {
        LlamadaRest.ejecutar(ejecutor, vista, "No se pudo retirar", () -> cliente.eliminarCelular(celular.getCodCelular()),
            r -> cargar());
    }
}
