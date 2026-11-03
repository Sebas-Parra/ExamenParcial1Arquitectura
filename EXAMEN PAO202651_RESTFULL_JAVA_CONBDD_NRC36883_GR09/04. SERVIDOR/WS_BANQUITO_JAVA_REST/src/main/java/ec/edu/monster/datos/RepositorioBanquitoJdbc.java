package ec.edu.monster.datos;

import ec.edu.monster.modelos.Cliente;
import ec.edu.monster.modelos.Credito;
import ec.edu.monster.modelos.CuotaAmortizacion;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Repositorio JDBC sobre la base MariaDB/MySQL "banquito" (ver 03. BDD/BANQUITO.sql). */
public class RepositorioBanquitoJdbc implements RepositorioBanquito {
    private static final String COLUMNAS_CLIENTE = "COD_CLIENTE, CEDULA, NOMBRE, GENERO, FECHA_NACIMIENTO";
    private static final String COLUMNAS_CREDITO =
        "COD_CREDITO, COD_CLIENTE, MONTO, NUMERO_CUOTAS, TASA_ANUAL, VALOR_CUOTA, FECHA, ESTADO";

    private final String url;
    private final String usuario;
    private final String clave;

    public RepositorioBanquitoJdbc() {
        this(System.getProperty("banquito.db.url", "jdbc:mariadb://localhost:3306/banquito"),
            System.getProperty("banquito.db.usuario", "banquito"),
            System.getProperty("banquito.db.clave", "banquito"));
    }

    public RepositorioBanquitoJdbc(String url, String usuario, String clave) {
        this.url = url;
        this.usuario = usuario;
        this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        try {
            // Dentro de un WAR el driver no siempre se registra solo con DriverManager.
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de MariaDB", e);
        }
        return DriverManager.getConnection(url, usuario, clave);
    }

    @Override
    public Cliente buscarClientePorCedula(String cedula) throws SQLException {
        return buscarCliente("SELECT " + COLUMNAS_CLIENTE + " FROM CLIENTE WHERE CEDULA = ?", cedula);
    }

    @Override
    public Cliente buscarClientePorCodigo(int codCliente) throws SQLException {
        return buscarCliente("SELECT " + COLUMNAS_CLIENTE + " FROM CLIENTE WHERE COD_CLIENTE = ?", codCliente);
    }

    private Cliente buscarCliente(String sql, Object parametro) throws SQLException {
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setObject(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Cliente(rs.getInt("COD_CLIENTE"), rs.getString("CEDULA"), rs.getString("NOMBRE"),
                    rs.getString("GENERO"), rs.getDate("FECHA_NACIMIENTO").toLocalDate());
            }
        }
    }

    @Override
    public boolean tieneDepositoDesde(int codCliente, LocalDate desde) throws SQLException {
        String sql = "SELECT 1 FROM MOVIMIENTO M JOIN CUENTA C ON C.NUM_CUENTA = M.NUM_CUENTA "
            + "WHERE C.COD_CLIENTE = ? AND M.TIPO = 'DEP' AND M.FECHA >= ? LIMIT 1";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            ps.setDate(2, Date.valueOf(desde));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public BigDecimal promedioMovimientos(int codCliente, String tipo, LocalDate desde) throws SQLException {
        String sql = "SELECT COALESCE(AVG(M.VALOR), 0) FROM MOVIMIENTO M JOIN CUENTA C ON C.NUM_CUENTA = M.NUM_CUENTA "
            + "WHERE C.COD_CLIENTE = ? AND M.TIPO = ? AND M.FECHA >= ?";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            ps.setString(2, tipo);
            ps.setDate(3, Date.valueOf(desde));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    @Override
    public boolean tieneCreditoActivo(int codCliente) throws SQLException {
        String sql = "SELECT 1 FROM CREDITO WHERE COD_CLIENTE = ? AND ESTADO = 'ACTIVO' LIMIT 1";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    @Override
    public int registrarCredito(Credito credito, List<CuotaAmortizacion> tabla) throws SQLException {
        String sqlCredito = "INSERT INTO CREDITO (COD_CLIENTE, MONTO, NUMERO_CUOTAS, TASA_ANUAL, VALOR_CUOTA, FECHA, ESTADO) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlCuota = "INSERT INTO TABLA_AMORTIZACION "
            + "(COD_CREDITO, NUMERO_CUOTA, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                int codCredito;
                try (PreparedStatement ps = con.prepareStatement(sqlCredito, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, credito.getCodCliente());
                    ps.setBigDecimal(2, credito.getMonto());
                    ps.setInt(3, credito.getNumeroCuotas());
                    ps.setBigDecimal(4, credito.getTasaAnual());
                    ps.setBigDecimal(5, credito.getValorCuota());
                    ps.setDate(6, Date.valueOf(credito.getFecha()));
                    ps.setString(7, credito.getEstado());
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        codCredito = claves.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlCuota)) {
                    for (CuotaAmortizacion cuota : tabla) {
                        ps.setInt(1, codCredito);
                        ps.setInt(2, cuota.getNumeroCuota());
                        ps.setBigDecimal(3, cuota.getValorCuota());
                        ps.setBigDecimal(4, cuota.getInteresPagado());
                        ps.setBigDecimal(5, cuota.getCapitalPagado());
                        ps.setBigDecimal(6, cuota.getSaldo());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                return codCredito;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public Credito buscarCredito(int codCredito) throws SQLException {
        List<Credito> creditos = consultarCreditos(
            "SELECT " + COLUMNAS_CREDITO + " FROM CREDITO WHERE COD_CREDITO = ?", codCredito);
        return creditos.isEmpty() ? null : creditos.get(0);
    }

    @Override
    public List<Credito> listarCreditos(int codCliente) throws SQLException {
        return consultarCreditos(
            "SELECT " + COLUMNAS_CREDITO + " FROM CREDITO WHERE COD_CLIENTE = ? ORDER BY COD_CREDITO DESC", codCliente);
    }

    private List<Credito> consultarCreditos(String sql, int parametro) throws SQLException {
        List<Credito> creditos = new ArrayList<>();
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    creditos.add(new Credito(rs.getInt("COD_CREDITO"), rs.getInt("COD_CLIENTE"),
                        rs.getBigDecimal("MONTO"), rs.getInt("NUMERO_CUOTAS"), rs.getBigDecimal("TASA_ANUAL"),
                        rs.getBigDecimal("VALOR_CUOTA"), rs.getDate("FECHA").toLocalDate().toString(),
                        rs.getString("ESTADO")));
                }
            }
        }
        return creditos;
    }

    @Override
    public List<CuotaAmortizacion> obtenerTablaAmortizacion(int codCredito) throws SQLException {
        String sql = "SELECT NUMERO_CUOTA, VALOR_CUOTA, INTERES_PAGADO, CAPITAL_PAGADO, SALDO "
            + "FROM TABLA_AMORTIZACION WHERE COD_CREDITO = ? ORDER BY NUMERO_CUOTA";
        List<CuotaAmortizacion> tabla = new ArrayList<>();
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codCredito);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabla.add(new CuotaAmortizacion(rs.getInt("NUMERO_CUOTA"), rs.getBigDecimal("VALOR_CUOTA"),
                        rs.getBigDecimal("INTERES_PAGADO"), rs.getBigDecimal("CAPITAL_PAGADO"),
                        rs.getBigDecimal("SALDO")));
                }
            }
        }
        return tabla;
    }
}
