package ec.edu.monster.controlador;

import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.vista.VentanaPrincipal;

/** Arma la ventana principal, sus controladores y la navegación entre secciones. */
public final class ControladorPrincipal {
    private final VentanaPrincipal ventana;
    private final ControladorCatalogo catalogo;
    private final ControladorFacturacion facturacion;
    private final ControladorConsultas consultas;

    public ControladorPrincipal(ClienteRest cliente, VentanaPrincipal ventana) {
        this.ventana = ventana;
        this.catalogo = new ControladorCatalogo(cliente, ventana);
        this.facturacion = new ControladorFacturacion(cliente, ventana);
        this.consultas = new ControladorConsultas(cliente, ventana);
        ventana.alCambiarSeccion(this::mostrar);
    }

    public void iniciar() {
        ventana.setVisible(true);
        mostrar(VentanaPrincipal.CATALOGO);
    }

    /** Cambia de sección y recarga sus datos para reflejar cambios hechos desde otros clientes. */
    private void mostrar(String seccion) {
        ventana.mostrarSeccion(seccion);
        switch (seccion) {
            case VentanaPrincipal.CATALOGO -> catalogo.cargar();
            case VentanaPrincipal.FACTURAR -> facturacion.cargarCelulares();
            case VentanaPrincipal.FACTURAS -> consultas.buscarFacturas();
            default -> ventana.mostrarEstado(" ");
        }
    }
}
