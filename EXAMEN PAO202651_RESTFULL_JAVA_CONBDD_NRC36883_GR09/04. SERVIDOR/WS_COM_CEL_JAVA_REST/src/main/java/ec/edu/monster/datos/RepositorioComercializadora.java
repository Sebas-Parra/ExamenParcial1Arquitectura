package ec.edu.monster.datos;

import ec.edu.monster.modelos.Celular;
import ec.edu.monster.modelos.Factura;
import java.sql.SQLException;
import java.util.List;

/** Acceso a los datos del catálogo y la facturación de la Comercializadora de Teléfonos Celulares. */
public interface RepositorioComercializadora {
    /** Celulares del catálogo; si soloDisponibles es true se omiten los retirados. */
    List<Celular> listarCelulares(boolean soloDisponibles) throws SQLException;

    Celular buscarCelular(int codCelular) throws SQLException;

    int crearCelular(Celular celular) throws SQLException;

    void actualizarCelular(Celular celular) throws SQLException;

    /** Retira el celular del catálogo (DISPONIBLE = 0) sin afectar las facturas que lo incluyen. */
    void retirarCelular(int codCelular) throws SQLException;

    /** Guarda el comprador, la cabecera y el detalle en una sola transacción y devuelve el código de factura. */
    int registrarFactura(Factura factura) throws SQLException;

    Factura buscarFactura(int codFactura) throws SQLException;

    /** Facturas ordenadas de la más reciente a la más antigua; si cedula es null se listan todas. */
    List<Factura> listarFacturas(String cedula) throws SQLException;
}
