package ec.edu.monster.servicio;

import java.io.IOException;

/**
 * Ejecuta llamadas de red fuera del hilo principal y entrega el resultado en él.
 * Los controladores dependen de esta interfaz y no de las clases de Android.
 */
public interface EjecutorTareas {

    <T> void ejecutar(Tarea<T> tarea, Resultado<T> resultado);

    /** Cancela las tareas pendientes; sus resultados ya no se entregan. */
    void cerrar();

    interface Tarea<T> {
        T ejecutar() throws IOException;
    }

    interface Resultado<T> {
        void exito(T valor);

        void error(IOException ex);
    }
}
