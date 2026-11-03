package ec.edu.monster.controlador;

import ec.edu.monster.modelo.Celular;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.DialogoCelular;
import ec.edu.monster.vista.PanelCatalogo;
import ec.edu.monster.vista.VentanaPrincipal;
import javax.swing.JOptionPane;

/** Mantenimiento del catálogo de celulares. */
public final class ControladorCatalogo {
    private final ClienteRest cliente;
    private final VentanaPrincipal ventana;
    private final PanelCatalogo panel;

    public ControladorCatalogo(ClienteRest cliente, VentanaPrincipal ventana) {
        this.cliente = cliente;
        this.ventana = ventana;
        this.panel = ventana.catalogo();
        panel.alRecargar(e -> cargar());
        panel.alNuevo(e -> nuevo());
        panel.alEditar(e -> editar());
        panel.alRetirar(e -> retirar());
    }

    public void cargar() {
        boolean todos = panel.incluirRetirados();
        TareaRest.ejecutar(ventana, "Consultando el catálogo…", "Catálogo",
            () -> cliente.listarCelulares(todos), r -> {
                panel.setCelulares(r.getCelulares());
                ventana.mostrarEstado(r.getCelulares().size() + " celulares en el catálogo.");
            });
    }

    private void nuevo() {
        Celular celular = DialogoCelular.mostrar(ventana, null);
        if (celular == null) return;
        TareaRest.ejecutar(ventana, "Registrando celular…", "No se pudo registrar",
            () -> cliente.crearCelular(celular), r -> {
                ventana.mostrarAviso(r.getCelular().nombre() + " se registró con el código "
                    + r.getCelular().getCodCelular() + ".", "Celular registrado", JOptionPane.INFORMATION_MESSAGE);
                cargar();
            });
    }

    private void editar() {
        Celular actual = panel.seleccionado();
        if (actual == null) {
            ventana.mostrarAviso("Selecciona el celular que deseas editar.", "Editar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Celular cambios = DialogoCelular.mostrar(ventana, actual);
        if (cambios == null) return;
        TareaRest.ejecutar(ventana, "Guardando cambios…", "No se pudo actualizar",
            () -> cliente.actualizarCelular(actual.getCodCelular(), cambios), r -> cargar());
    }

    private void retirar() {
        Celular celular = panel.seleccionado();
        if (celular == null) {
            ventana.mostrarAviso("Selecciona el celular que deseas retirar.", "Retirar", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Boolean.FALSE.equals(celular.getDisponible())) {
            ventana.mostrarAviso("El celular ya está retirado. Puedes volver a ofrecerlo desde Editar.", "Retirar",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!ventana.confirmar("¿Retirar " + celular.nombre() + " del catálogo?\nSus facturas se conservan.",
                "Retirar celular")) return;
        TareaRest.ejecutar(ventana, "Retirando celular…", "No se pudo retirar",
            () -> cliente.eliminarCelular(celular.getCodCelular()), r -> cargar());
    }
}
