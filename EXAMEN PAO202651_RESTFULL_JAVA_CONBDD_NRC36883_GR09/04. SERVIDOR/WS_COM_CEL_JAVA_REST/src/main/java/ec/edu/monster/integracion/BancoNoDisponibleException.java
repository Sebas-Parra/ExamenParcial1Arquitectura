package ec.edu.monster.integracion;

/** No se pudo invocar el servicio web del Banco BanQuito o su respuesta no es válida. */
public class BancoNoDisponibleException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public BancoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
