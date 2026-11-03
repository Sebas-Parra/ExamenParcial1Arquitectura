package ec.edu.monster.datos;

import ec.edu.monster.modelos.Cliente;
import ec.edu.monster.modelos.Credito;
import ec.edu.monster.modelos.CuotaAmortizacion;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Acceso a los datos del Core BanQuito y del módulo de crédito. */
public interface RepositorioBanquito {
    Cliente buscarClientePorCedula(String cedula) throws SQLException;

    Cliente buscarClientePorCodigo(int codCliente) throws SQLException;

    boolean tieneDepositoDesde(int codCliente, LocalDate desde) throws SQLException;

    /** Promedio del valor de los movimientos del tipo indicado (DEP o RET) desde la fecha dada; 0 si no hay. */
    BigDecimal promedioMovimientos(int codCliente, String tipo, LocalDate desde) throws SQLException;

    boolean tieneCreditoActivo(int codCliente) throws SQLException;

    /** Guarda el crédito y su tabla de amortización en una sola transacción y devuelve el código generado. */
    int registrarCredito(Credito credito, List<CuotaAmortizacion> tabla) throws SQLException;

    Credito buscarCredito(int codCredito) throws SQLException;

    List<CuotaAmortizacion> obtenerTablaAmortizacion(int codCredito) throws SQLException;

    List<Credito> listarCreditos(int codCliente) throws SQLException;
}
