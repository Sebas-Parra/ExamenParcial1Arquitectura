package ec.edu.monster.vista;

import ec.edu.monster.modelo.Celular;
import java.util.List;

/** Lo que el controlador del catálogo necesita de la pantalla. */
public interface VistaCatalogo extends VistaBase {
    boolean incluirRetirados();

    void mostrarCelulares(List<Celular> celulares);
}
