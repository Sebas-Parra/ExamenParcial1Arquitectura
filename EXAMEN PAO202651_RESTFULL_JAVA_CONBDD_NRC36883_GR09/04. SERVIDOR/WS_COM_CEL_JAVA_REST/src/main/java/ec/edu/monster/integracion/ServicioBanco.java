package ec.edu.monster.integracion;

import ec.edu.monster.modelos.Respuesta;
import java.math.BigDecimal;

/**
 * Servicios web del módulo de crédito del Banco BanQuito que consume la Comercializadora.
 * Cada método devuelve la respuesta del banco tal cual (también cuando es un error de negocio)
 * y lanza {@link BancoNoDisponibleException} si no se pudo comunicar con él.
 */
public interface ServicioBanco {
    Respuesta verificarSujetoCredito(String cedula);

    Respuesta obtenerMontoMaximo(int codCliente);

    Respuesta otorgarCredito(int codCliente, BigDecimal precio, int numeroCuotas);

    Respuesta obtenerTablaAmortizacion(int codCredito);

    Respuesta listarCreditos(String cedula);
}
