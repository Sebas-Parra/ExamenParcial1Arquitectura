package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.vista.Formato;
import ec.edu.monster.vista.VentanaPrincipal;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/** Ejecuta una llamada REST fuera del hilo de Swing y presenta los errores de forma uniforme. */
final class TareaRest {
    interface Llamada {
        RespuestaRest ejecutar() throws IOException;
    }

    private TareaRest() { }

    /** Llama al servidor; si responde OK invoca alExito, si no muestra el error con el título indicado. */
    static void ejecutar(VentanaPrincipal ventana, String estado, String tituloError, Llamada llamada,
            Consumer<RespuestaRest> alExito) {
        ventana.setOcupado(true);
        ventana.mostrarEstado(estado);
        new SwingWorker<RespuestaRest, Void>() {
            @Override protected RespuestaRest doInBackground() throws IOException {
                return llamada.ejecutar();
            }

            @Override protected void done() {
                ventana.setOcupado(false);
                try {
                    RespuestaRest respuesta = get();
                    if (respuesta.isExito()) {
                        ventana.mostrarEstado(respuesta.getMensaje());
                        alExito.accept(respuesta);
                    } else {
                        ventana.mostrarEstado("No se pudo completar la operación.");
                        ventana.mostrarAviso(mensaje(respuesta), tituloError, JOptionPane.WARNING_MESSAGE);
                    }
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    ventana.mostrarEstado("Se interrumpió la operación.");
                } catch (ExecutionException ex) {
                    Throwable causa = ex.getCause() == null ? ex : ex.getCause();
                    ventana.mostrarEstado("No se pudo comunicar con el servidor.");
                    ventana.mostrarAviso(causa.getMessage(), "Error de comunicación", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    /** Texto de un error del servidor, con el motivo del banco o el monto máximo cuando vienen. */
    static String mensaje(RespuestaRest r) {
        StringBuilder texto = new StringBuilder();
        texto.append(r.getMensaje() == null || r.getMensaje().isBlank() ? "El servidor no envió un mensaje." : r.getMensaje());
        if (r.getMotivo() != null) texto.append("\nMotivo: ").append(r.getMotivo());
        if (r.getMontoMaximo() != null)
            texto.append("\nMonto máximo de crédito aprobado: ").append(Formato.dinero(r.getMontoMaximo()));
        texto.append("\n\nCódigo: ").append(r.getCodigo() == null ? "HTTP " + r.getEstadoHttp() : r.getCodigo());
        return texto.toString();
    }
}
