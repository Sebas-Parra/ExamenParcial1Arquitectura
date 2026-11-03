package ec.edu.monster.vista;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorCatalogo;
import ec.edu.monster.modelo.Celular;
import java.util.List;

/** Pantalla de mantenimiento del catálogo de celulares. */
final class PantallaCatalogo implements VistaCatalogo {
    private final PrincipalActivity actividad;
    private final View raiz;
    private final LinearLayout lista;
    private final MaterialSwitch retirados;
    private ControladorCatalogo controlador;

    PantallaCatalogo(PrincipalActivity actividad) {
        this.actividad = actividad;
        this.raiz = actividad.getLayoutInflater().inflate(R.layout.pantalla_catalogo, null);
        this.lista = raiz.findViewById(R.id.lista);
        this.retirados = raiz.findViewById(R.id.retirados);
    }

    void conectar(ControladorCatalogo controlador) {
        this.controlador = controlador;
        raiz.findViewById(R.id.nuevo).setOnClickListener(v ->
            DialogoCelular.mostrar(actividad, null, datos -> controlador.guardar(null, datos)));
        retirados.setOnCheckedChangeListener((boton, marcado) -> controlador.cargar());
    }

    View vista() {
        return raiz;
    }

    @Override public boolean incluirRetirados() { return retirados.isChecked(); }
    @Override public void setOcupado(boolean ocupado) { actividad.setOcupado(ocupado); }
    @Override public void mostrarAviso(String titulo, String mensaje) { actividad.mostrarAviso(titulo, mensaje); }

    @Override
    public void mostrarCelulares(List<Celular> celulares) {
        lista.removeAllViews();
        if (celulares.isEmpty()) {
            lista.addView(actividad.textoVacio(R.string.sin_celulares));
            return;
        }
        for (Celular c : celulares) {
            View fila = actividad.getLayoutInflater().inflate(R.layout.item_celular, lista, false);
            boolean retirado = Boolean.FALSE.equals(c.getDisponible());
            ((TextView) fila.findViewById(R.id.nombre)).setText(c.getCodCelular() + " · " + c.nombre());
            ((TextView) fila.findViewById(R.id.precio)).setText(Formato.dinero(c.getPrecio()));
            TextView descripcion = fila.findViewById(R.id.descripcion);
            descripcion.setText(c.getDescripcion());
            descripcion.setVisibility(c.getDescripcion() == null ? View.GONE : View.VISIBLE);
            TextView estado = fila.findViewById(R.id.estado);
            estado.setText(retirado ? "Retirado" : "Disponible");
            estado.setTextColor(retirado ? ContextCompat.getColor(actividad, R.color.gris) : 0xFF17633E);
            fila.findViewById(R.id.editar).setOnClickListener(v ->
                DialogoCelular.mostrar(actividad, c, datos -> controlador.guardar(c.getCodCelular(), datos)));
            View retirar = fila.findViewById(R.id.retirar);
            retirar.setVisibility(retirado ? View.GONE : View.VISIBLE);
            retirar.setOnClickListener(v -> new MaterialAlertDialogBuilder(actividad)
                .setTitle("Retirar celular")
                .setMessage("¿Retirar " + c.nombre() + " del catálogo? Sus facturas se conservan.")
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.retirar, (d, b) -> controlador.retirar(c))
                .show());
            lista.addView(fila);
        }
    }
}
