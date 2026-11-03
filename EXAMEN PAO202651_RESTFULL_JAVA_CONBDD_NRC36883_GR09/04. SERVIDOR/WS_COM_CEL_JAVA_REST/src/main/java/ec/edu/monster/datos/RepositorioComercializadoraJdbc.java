package ec.edu.monster.datos;

import ec.edu.monster.modelos.Celular;
import ec.edu.monster.modelos.DetalleFactura;
import ec.edu.monster.modelos.Factura;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Repositorio JDBC sobre la base MariaDB/MySQL "comercializadora" (ver 03. BDD/COMERCIALIZADORA.sql). */
public class RepositorioComercializadoraJdbc implements RepositorioComercializadora {
    private static final String COLUMNAS_CELULAR = "COD_CELULAR, MARCA, MODELO, DESCRIPCION, PRECIO, DISPONIBLE";
    private static final String CONSULTA_FACTURA = "SELECT F.COD_FACTURA, F.CEDULA, C.NOMBRE, F.FECHA, F.FORMA_PAGO, "
        + "F.SUBTOTAL, F.DESCUENTO, F.TOTAL, F.COD_CREDITO, F.NUMERO_CUOTAS, F.VALOR_CUOTA "
        + "FROM FACTURA F JOIN COMPRADOR C ON C.CEDULA = F.CEDULA";

    private final String url;
    private final String usuario;
    private final String clave;

    public RepositorioComercializadoraJdbc() {
        this(System.getProperty("comercializadora.db.url", "jdbc:mariadb://localhost:3306/comercializadora"),
            System.getProperty("comercializadora.db.usuario", "comercializadora"),
            System.getProperty("comercializadora.db.clave", "comercializadora"));
    }

    public RepositorioComercializadoraJdbc(String url, String usuario, String clave) {
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
    public List<Celular> listarCelulares(boolean soloDisponibles) throws SQLException {
        String sql = "SELECT " + COLUMNAS_CELULAR + " FROM CELULAR"
            + (soloDisponibles ? " WHERE DISPONIBLE = 1" : "") + " ORDER BY MARCA, MODELO";
        List<Celular> celulares = new ArrayList<>();
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) celulares.add(celular(rs));
        }
        return celulares;
    }

    @Override
    public Celular buscarCelular(int codCelular) throws SQLException {
        String sql = "SELECT " + COLUMNAS_CELULAR + " FROM CELULAR WHERE COD_CELULAR = ?";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codCelular);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? celular(rs) : null;
            }
        }
    }

    private static Celular celular(ResultSet rs) throws SQLException {
        return new Celular(rs.getInt("COD_CELULAR"), rs.getString("MARCA"), rs.getString("MODELO"),
            rs.getString("DESCRIPCION"), rs.getBigDecimal("PRECIO"), rs.getBoolean("DISPONIBLE"));
    }

    @Override
    public int crearCelular(Celular celular) throws SQLException {
        String sql = "INSERT INTO CELULAR (MARCA, MODELO, DESCRIPCION, PRECIO, DISPONIBLE) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conectar();
                PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, celular.getMarca());
            ps.setString(2, celular.getModelo());
            ps.setString(3, celular.getDescripcion());
            ps.setBigDecimal(4, celular.getPrecio());
            ps.setBoolean(5, celular.getDisponible());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                claves.next();
                return claves.getInt(1);
            }
        }
    }

    @Override
    public void actualizarCelular(Celular celular) throws SQLException {
        String sql = "UPDATE CELULAR SET MARCA = ?, MODELO = ?, DESCRIPCION = ?, PRECIO = ?, DISPONIBLE = ? "
            + "WHERE COD_CELULAR = ?";
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, celular.getMarca());
            ps.setString(2, celular.getModelo());
            ps.setString(3, celular.getDescripcion());
            ps.setBigDecimal(4, celular.getPrecio());
            ps.setBoolean(5, celular.getDisponible());
            ps.setInt(6, celular.getCodCelular());
            ps.executeUpdate();
        }
    }

    @Override
    public void retirarCelular(int codCelular) throws SQLException {
        try (Connection con = conectar();
                PreparedStatement ps = con.prepareStatement("UPDATE CELULAR SET DISPONIBLE = 0 WHERE COD_CELULAR = ?")) {
            ps.setInt(1, codCelular);
            ps.executeUpdate();
        }
    }

    @Override
    public int registrarFactura(Factura factura) throws SQLException {
        String sqlComprador = "INSERT INTO COMPRADOR (CEDULA, NOMBRE) VALUES (?, ?) "
            + "ON DUPLICATE KEY UPDATE NOMBRE = VALUES(NOMBRE)";
        String sqlFactura = "INSERT INTO FACTURA (CEDULA, FECHA, FORMA_PAGO, SUBTOTAL, DESCUENTO, TOTAL, "
            + "COD_CREDITO, NUMERO_CUOTAS, VALOR_CUOTA) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO DETALLE_FACTURA (COD_FACTURA, COD_CELULAR, CANTIDAD, PRECIO_UNITARIO, SUBTOTAL) "
            + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = conectar()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(sqlComprador)) {
                    ps.setString(1, factura.getCedula());
                    ps.setString(2, factura.getNombre());
                    ps.executeUpdate();
                }
                int codFactura;
                try (PreparedStatement ps = con.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, factura.getCedula());
                    ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.parse(factura.getFecha())));
                    ps.setString(3, factura.getFormaPago());
                    ps.setBigDecimal(4, factura.getSubtotal());
                    ps.setBigDecimal(5, factura.getDescuento());
                    ps.setBigDecimal(6, factura.getTotal());
                    ps.setObject(7, factura.getCodCredito(), Types.INTEGER);
                    ps.setObject(8, factura.getNumeroCuotas(), Types.INTEGER);
                    ps.setBigDecimal(9, factura.getValorCuota());
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        codFactura = claves.getInt(1);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                    for (DetalleFactura detalle : factura.getDetalles()) {
                        ps.setInt(1, codFactura);
                        ps.setInt(2, detalle.getCodCelular());
                        ps.setInt(3, detalle.getCantidad());
                        ps.setBigDecimal(4, detalle.getPrecioUnitario());
                        ps.setBigDecimal(5, detalle.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                return codFactura;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    @Override
    public Factura buscarFactura(int codFactura) throws SQLException {
        try (Connection con = conectar();
                PreparedStatement ps = con.prepareStatement(CONSULTA_FACTURA + " WHERE F.COD_FACTURA = ?")) {
            ps.setInt(1, codFactura);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Factura factura = factura(rs);
                factura.setDetalles(detalles(con, codFactura));
                return factura;
            }
        }
    }

    @Override
    public List<Factura> listarFacturas(String cedula) throws SQLException {
        String sql = CONSULTA_FACTURA + (cedula == null ? "" : " WHERE F.CEDULA = ?") + " ORDER BY F.COD_FACTURA DESC";
        List<Factura> facturas = new ArrayList<>();
        try (Connection con = conectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            if (cedula != null) ps.setString(1, cedula);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) facturas.add(factura(rs));
            }
            for (Factura factura : facturas) factura.setDetalles(detalles(con, factura.getCodFactura()));
        }
        return facturas;
    }

    private static Factura factura(ResultSet rs) throws SQLException {
        Factura factura = new Factura();
        factura.setCodFactura(rs.getInt("COD_FACTURA"));
        factura.setCedula(rs.getString("CEDULA"));
        factura.setNombre(rs.getString("NOMBRE"));
        factura.setFecha(rs.getTimestamp("FECHA").toLocalDateTime().withNano(0).toString());
        factura.setFormaPago(rs.getString("FORMA_PAGO"));
        factura.setSubtotal(rs.getBigDecimal("SUBTOTAL"));
        factura.setDescuento(rs.getBigDecimal("DESCUENTO"));
        factura.setTotal(rs.getBigDecimal("TOTAL"));
        factura.setCodCredito(rs.getObject("COD_CREDITO", Integer.class));
        factura.setNumeroCuotas(rs.getObject("NUMERO_CUOTAS", Integer.class));
        factura.setValorCuota(rs.getBigDecimal("VALOR_CUOTA"));
        return factura;
    }

    private static List<DetalleFactura> detalles(Connection con, int codFactura) throws SQLException {
        String sql = "SELECT D.COD_CELULAR, C.MARCA, C.MODELO, D.CANTIDAD, D.PRECIO_UNITARIO, D.SUBTOTAL "
            + "FROM DETALLE_FACTURA D JOIN CELULAR C ON C.COD_CELULAR = D.COD_CELULAR "
            + "WHERE D.COD_FACTURA = ? ORDER BY D.COD_CELULAR";
        List<DetalleFactura> detalles = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, codFactura);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    detalles.add(new DetalleFactura(rs.getInt("COD_CELULAR"), rs.getString("MARCA"),
                        rs.getString("MODELO"), rs.getInt("CANTIDAD"), rs.getBigDecimal("PRECIO_UNITARIO"),
                        rs.getBigDecimal("SUBTOTAL")));
                }
            }
        }
        return detalles;
    }
}
