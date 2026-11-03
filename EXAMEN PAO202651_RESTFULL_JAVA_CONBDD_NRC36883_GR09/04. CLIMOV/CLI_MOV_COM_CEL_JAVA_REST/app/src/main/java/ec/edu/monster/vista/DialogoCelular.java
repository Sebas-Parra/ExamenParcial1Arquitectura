package ec.edu.monster.vista;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorCatalogo;
import ec.edu.monster.modelo.Celular;
import java.math.BigDecimal;

/** Formulario para registrar o editar un celular; solo se cierra cuando los datos son válidos. */
final class DialogoCelular {
    interface AlGuardar {
        void guardar(Celular datos);
    }

    private DialogoCelular() { }

    /** actual es null para un celular nuevo. */
    static void mostrar(Activity actividad, Celular actual, AlGuardar alGuardar) {
        View vista = actividad.getLayoutInflater().inflate(R.layout.dialogo_celular, null);
        EditText marca = vista.findViewById(R.id.marca);
        EditText modelo = vista.findViewById(R.id.modelo);
        EditText descripcion = vista.findViewById(R.id.descripcion);
        EditText precio = vista.findViewById(R.id.precio);
        CheckBox disponible = vista.findViewById(R.id.disponible);
        if (actual != null) {
            marca.setText(actual.getMarca());
            modelo.setText(actual.getModelo());
            descripcion.setText(actual.getDescripcion());
            precio.setText(actual.getPrecio().toPlainString());
            disponible.setVisibility(View.VISIBLE);
            disponible.setChecked(!Boolean.FALSE.equals(actual.getDisponible()));
        }
        AlertDialog dialogo = new MaterialAlertDialogBuilder(actividad)
            .setTitle(actual == null ? R.string.nuevo_celular : R.string.editar_celular)
            .setView(vista)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.guardar, null)
            .create();
        dialogo.setOnShowListener(d -> dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(boton -> {
            String error = ControladorCatalogo.validar(marca.getText().toString(), modelo.getText().toString(),
                precio.getText().toString());
            if (error != null) {
                new MaterialAlertDialogBuilder(actividad).setTitle("Datos incompletos").setMessage(error)
                    .setPositiveButton(R.string.aceptar, null).show();
                return;
            }
            String textoDescripcion = descripcion.getText().toString().trim();
            Celular datos = new Celular(null, marca.getText().toString().trim(), modelo.getText().toString().trim(),
                textoDescripcion.isEmpty() ? null : textoDescripcion,
                new BigDecimal(precio.getText().toString().trim().replace(',', '.')),
                actual == null ? null : disponible.isChecked());
            dialogo.dismiss();
            alGuardar.guardar(datos);
        }));
        dialogo.show();
    }
}
