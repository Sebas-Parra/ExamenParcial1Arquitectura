package ec.edu.monster.vista;

import android.view.View;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/** Respeta barra de estado, navegación y teclado cuando la ventana usa pantalla completa. */
final class AjustesVentana {

    private AjustesVentana() { }

    /**
     * El margen superior va a la cabecera (si existe) para que su color llegue bajo la barra de estado;
     * los demás márgenes van a la raíz.
     */
    static void aplicarMargenes(View raiz, View cabecera) {
        final int raizIzquierda = raiz.getPaddingLeft();
        final int raizArriba = raiz.getPaddingTop();
        final int raizDerecha = raiz.getPaddingRight();
        final int raizAbajo = raiz.getPaddingBottom();
        final int cabeceraArriba = cabecera == null ? 0 : cabecera.getPaddingTop();

        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, insets) -> {
            Insets margen = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            if (cabecera == null) {
                vista.setPadding(raizIzquierda + margen.left, raizArriba + margen.top,
                        raizDerecha + margen.right, raizAbajo + margen.bottom);
            } else {
                vista.setPadding(raizIzquierda + margen.left, raizArriba,
                        raizDerecha + margen.right, raizAbajo + margen.bottom);
                cabecera.setPadding(cabecera.getPaddingLeft(), cabeceraArriba + margen.top,
                        cabecera.getPaddingRight(), cabecera.getPaddingBottom());
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }
}
