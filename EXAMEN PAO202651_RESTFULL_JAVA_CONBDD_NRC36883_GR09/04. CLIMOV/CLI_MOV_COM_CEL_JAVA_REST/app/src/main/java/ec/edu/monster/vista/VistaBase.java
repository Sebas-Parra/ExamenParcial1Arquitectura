package ec.edu.monster.vista;

/** Lo común que los controladores necesitan de cualquier pantalla, sin depender de Android. */
public interface VistaBase {
    void setOcupado(boolean ocupado);

    void mostrarAviso(String titulo, String mensaje);
}
