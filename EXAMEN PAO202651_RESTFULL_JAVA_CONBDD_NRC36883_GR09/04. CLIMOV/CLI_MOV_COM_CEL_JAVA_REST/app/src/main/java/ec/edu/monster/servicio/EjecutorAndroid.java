package ec.edu.monster.servicio;

import android.os.Handler;
import android.os.Looper;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/** Hilo de fondo para la red; el resultado vuelve al hilo principal de Android. */
public final class EjecutorAndroid implements EjecutorTareas {
    private final ExecutorService fondo = Executors.newSingleThreadExecutor();
    private final Handler principal = new Handler(Looper.getMainLooper());
    private volatile boolean cerrado;

    @Override
    public <T> void ejecutar(Tarea<T> tarea, Resultado<T> resultado) {
        try {
            fondo.execute(() -> {
                try {
                    T valor = tarea.ejecutar();
                    entregar(() -> resultado.exito(valor));
                } catch (IOException ex) {
                    entregar(() -> resultado.error(ex));
                } catch (RuntimeException ex) {
                    IOException error = new IOException("No se pudo completar la operación. Inténtalo nuevamente.", ex);
                    entregar(() -> resultado.error(error));
                }
            });
        } catch (RejectedExecutionException ignorado) {
            // La pantalla ya se cerró.
        }
    }

    @Override
    public void cerrar() {
        cerrado = true;
        fondo.shutdownNow();
        principal.removeCallbacksAndMessages(null);
    }

    private void entregar(Runnable accion) {
        principal.post(() -> {
            if (!cerrado) {
                accion.run();
            }
        });
    }
}
