package ec.edu.monster.controlador;

import ec.edu.monster.modelo.RespuestaRest;
import ec.edu.monster.servicio.EjecutorTareas;
import ec.edu.monster.vista.Formato;
import ec.edu.monster.vista.VistaBase;
import java.io.IOException;

/** Ejecuta una llamada REST fuera del hilo principal y presenta los errores de forma uniforme. */
final class LlamadaRest {
    interface AlExito {
        void aceptar(RespuestaRest respuesta);
    }

    private LlamadaRest() { }

    /** Llama al servidor; si responde OK invoca alExito, si no muestra el error con el título indicado. */
    static void ejecutar(EjecutorTareas ejecutor, VistaBase vista, String tituloError,
            EjecutorTareas.Tarea<RespuestaRest> llamada, AlExito alExito) {
        vista.setOcupado(true);
        ejecutor.ejecutar(llamada, new EjecutorTareas.Resultado<RespuestaRest>() {
            @Override public void exito(RespuestaRest respuesta) {
                vista.setOcupado(false);
                if (respuesta.isExito()) alExito.aceptar(respuesta);
                else vista.mostrarAviso(tituloError, mensaje(respuesta));
            }

            @Override public void error(IOException ex) {
                vista.setOcupado(false);
                vista.mostrarAviso("Error de comunicación", ex.getMessage());
            }
        });
    }

    /** Texto de un error del servidor, con el motivo del banco o el monto máximo cuando vienen. */
    static String mensaje(RespuestaRest r) {
        StringBuilder texto = new StringBuilder(r.getMensaje());
        if (r.getMotivo() != null) texto.append("\n\nMotivo: ").append(r.getMotivo());
        if (r.getMontoMaximo() != null)
            texto.append("\n\nMonto máximo de crédito aprobado: ").append(Formato.dinero(r.getMontoMaximo()));
        texto.append("\n\nCódigo: ").append(r.getCodigo());
        return texto.toString();
    }
}
