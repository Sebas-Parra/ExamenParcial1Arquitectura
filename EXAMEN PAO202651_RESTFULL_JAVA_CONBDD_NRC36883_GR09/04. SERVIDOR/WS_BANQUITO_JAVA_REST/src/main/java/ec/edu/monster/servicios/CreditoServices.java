package ec.edu.monster.servicios;

import ec.edu.monster.datos.RepositorioBanquito;
import ec.edu.monster.datos.RepositorioBanquitoJdbc;
import ec.edu.monster.modelos.Cliente;
import ec.edu.monster.modelos.Credito;
import ec.edu.monster.modelos.CuotaAmortizacion;
import ec.edu.monster.modelos.Respuesta;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Lógica de negocio del módulo de crédito del Banco BanQuito, independiente del transporte REST. */
@ApplicationScoped
public class CreditoServices {
    public static final BigDecimal TASA_ANUAL = new BigDecimal("16.50");
    public static final int CUOTAS_MINIMAS = 3;
    public static final int CUOTAS_MAXIMAS = 18;
    public static final int EDAD_MINIMA_HOMBRES = 25;
    private static final BigDecimal MESES_MONTO_MAXIMO = BigDecimal.valueOf(6);
    private static final MathContext PRECISION = MathContext.DECIMAL128;

    private final RepositorioBanquito repositorio;
    private final Clock reloj;
    private final BigDecimal porcentajeMontoMaximo;

    public CreditoServices() {
        // El enunciado dice 35% en el texto y 30% en la fórmula; se usa la fórmula por defecto.
        this(new RepositorioBanquitoJdbc(), Clock.systemDefaultZone(),
            new BigDecimal(System.getProperty("banquito.credito.porcentaje", "0.30")));
    }

    public CreditoServices(RepositorioBanquito repositorio, Clock reloj, BigDecimal porcentajeMontoMaximo) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.reloj = Objects.requireNonNull(reloj);
        this.porcentajeMontoMaximo = Objects.requireNonNull(porcentajeMontoMaximo);
    }

    /** Verifica, a partir de la cédula, si una persona es sujeto de crédito. */
    public Respuesta verificarSujetoCredito(String cedula) {
        if (cedula == null || cedula.isBlank())
            return Respuesta.error("DATA-001", "Indique la cedula");
        try {
            Cliente cliente = repositorio.buscarClientePorCedula(cedula.trim());
            Respuesta respuesta = Respuesta.exito("Verificacion realizada");
            if (cliente == null) {
                respuesta.setCedula(cedula.trim());
                respuesta.setSujetoCredito(false);
                respuesta.setMotivo("La persona no es cliente del banco");
                return respuesta;
            }
            String motivo = motivoRechazo(cliente);
            respuesta.copiarCliente(cliente);
            respuesta.setSujetoCredito(motivo == null);
            respuesta.setMotivo(motivo == null ? "Cumple todas las reglas de credito" : motivo);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Calcula el monto máximo de crédito a partir del código de cliente. */
    public Respuesta obtenerMontoMaximo(Integer codCliente) {
        if (codCliente == null || codCliente <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de cliente valido");
        try {
            Cliente cliente = repositorio.buscarClientePorCodigo(codCliente);
            if (cliente == null) return Respuesta.error("CLI-001", "Cliente inexistente");
            LocalDate desde = hoy().minusMonths(3);
            BigDecimal depositos = repositorio.promedioMovimientos(codCliente, "DEP", desde);
            BigDecimal retiros = repositorio.promedioMovimientos(codCliente, "RET", desde);
            Respuesta respuesta = Respuesta.exito("Monto maximo calculado");
            respuesta.copiarCliente(cliente);
            respuesta.setPromedioDepositos(redondear(depositos));
            respuesta.setPromedioRetiros(redondear(retiros));
            respuesta.setMontoMaximo(montoMaximo(depositos, retiros));
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Otorga el crédito y registra su tabla de amortización si el cliente cumple las reglas. */
    public synchronized Respuesta otorgarCredito(Integer codCliente, BigDecimal precio, Integer numeroCuotas) {
        if (codCliente == null || codCliente <= 0 || precio == null || precio.signum() <= 0 || numeroCuotas == null)
            return Respuesta.error("DATA-001",
                "Indique el codigo de cliente, un precio mayor que cero y el numero de cuotas");
        if (numeroCuotas < CUOTAS_MINIMAS || numeroCuotas > CUOTAS_MAXIMAS)
            return Respuesta.error("DATA-001",
                "El plazo debe estar entre " + CUOTAS_MINIMAS + " y " + CUOTAS_MAXIMAS + " meses");
        if (precio.scale() > 2)
            return Respuesta.error("DATA-001", "El precio admite como maximo dos decimales");
        try {
            Cliente cliente = repositorio.buscarClientePorCodigo(codCliente);
            if (cliente == null) return Respuesta.error("CLI-001", "Cliente inexistente");
            String motivo = motivoRechazo(cliente);
            if (motivo != null) {
                Respuesta rechazo = Respuesta.error("CRED-001", "El cliente no es sujeto de credito");
                rechazo.copiarCliente(cliente);
                rechazo.setSujetoCredito(false);
                rechazo.setMotivo(motivo);
                return rechazo;
            }
            LocalDate desde = hoy().minusMonths(3);
            BigDecimal maximo = montoMaximo(repositorio.promedioMovimientos(codCliente, "DEP", desde),
                repositorio.promedioMovimientos(codCliente, "RET", desde));
            if (precio.compareTo(maximo) > 0) {
                Respuesta rechazo = Respuesta.error("CRED-002",
                    "El precio supera el monto maximo de credito aprobado");
                rechazo.copiarCliente(cliente);
                rechazo.setMontoMaximo(maximo);
                return rechazo;
            }

            List<CuotaAmortizacion> tabla = calcularTablaAmortizacion(precio, numeroCuotas, TASA_ANUAL);
            Credito credito = new Credito(0, codCliente, precio.setScale(2), numeroCuotas, TASA_ANUAL,
                tabla.get(0).getValorCuota(), hoy().toString(), "ACTIVO");
            credito.setCodCredito(repositorio.registrarCredito(credito, tabla));

            Respuesta respuesta = Respuesta.exito("Credito aprobado");
            respuesta.copiarCliente(cliente);
            respuesta.copiarCredito(credito);
            respuesta.setMontoMaximo(maximo);
            respuesta.setTablaAmortizacion(tabla);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Devuelve la tabla de amortización de un crédito otorgado. */
    public Respuesta obtenerTablaAmortizacion(Integer codCredito) {
        if (codCredito == null || codCredito <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de credito valido");
        try {
            Credito credito = repositorio.buscarCredito(codCredito);
            if (credito == null) return Respuesta.error("CRED-003", "Credito inexistente");
            Respuesta respuesta = Respuesta.exito("Tabla de amortizacion");
            respuesta.copiarCliente(repositorio.buscarClientePorCodigo(credito.getCodCliente()));
            respuesta.copiarCredito(credito);
            respuesta.setTablaAmortizacion(repositorio.obtenerTablaAmortizacion(codCredito));
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Lista los créditos de un cliente a partir de su cédula. */
    public Respuesta listarCreditos(String cedula) {
        if (cedula == null || cedula.isBlank())
            return Respuesta.error("DATA-001", "Indique la cedula");
        try {
            Cliente cliente = repositorio.buscarClientePorCedula(cedula.trim());
            if (cliente == null) return Respuesta.error("CLI-001", "Cliente inexistente");
            Respuesta respuesta = Respuesta.exito("Creditos del cliente");
            respuesta.copiarCliente(cliente);
            respuesta.setCreditos(repositorio.listarCreditos(cliente.getCodCliente()));
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /**
     * Tabla de cuota fija: la cuota se redondea a centavos y el interés, el capital y el saldo
     * se calculan con precisión completa y se redondean solo al presentarlos (igual que el ejemplo del enunciado).
     */
    public static List<CuotaAmortizacion> calcularTablaAmortizacion(BigDecimal monto, int numeroCuotas,
            BigDecimal tasaAnual) {
        BigDecimal tasaPeriodo = tasaAnual.divide(BigDecimal.valueOf(1200), PRECISION);
        BigDecimal descuento = BigDecimal.ONE.subtract(
            BigDecimal.ONE.divide(BigDecimal.ONE.add(tasaPeriodo).pow(numeroCuotas, PRECISION), PRECISION));
        BigDecimal cuota = redondear(monto.multiply(tasaPeriodo, PRECISION).divide(descuento, PRECISION));

        List<CuotaAmortizacion> tabla = new ArrayList<>();
        BigDecimal saldo = monto;
        for (int numero = 1; numero <= numeroCuotas; numero++) {
            BigDecimal interes = saldo.multiply(tasaPeriodo, PRECISION);
            BigDecimal capital = cuota.subtract(interes, PRECISION);
            saldo = saldo.subtract(capital, PRECISION);
            BigDecimal saldoMostrado = numero == numeroCuotas ? BigDecimal.ZERO.setScale(2) : redondear(saldo);
            tabla.add(new CuotaAmortizacion(numero, cuota, redondear(interes), redondear(capital), saldoMostrado));
        }
        return tabla;
    }

    /** Devuelve null si el cliente es sujeto de crédito o el motivo por el que no lo es. */
    private String motivoRechazo(Cliente cliente) throws SQLException {
        LocalDate hoy = hoy();
        if (!repositorio.tieneDepositoDesde(cliente.getCodCliente(), hoy.minusMonths(1)))
            return "No registra depositos en el ultimo mes";
        if ("M".equalsIgnoreCase(cliente.getGenero())
                && Period.between(cliente.getFechaNacimiento(), hoy).getYears() < EDAD_MINIMA_HOMBRES)
            return "Los clientes de genero masculino deben tener al menos " + EDAD_MINIMA_HOMBRES + " anios";
        if (repositorio.tieneCreditoActivo(cliente.getCodCliente()))
            return "Ya tiene un credito activo en el banco";
        return null;
    }

    /** ((Promedio depósitos − Promedio retiros) × porcentaje) × 6; nunca negativo. */
    private BigDecimal montoMaximo(BigDecimal depositos, BigDecimal retiros) {
        BigDecimal monto = depositos.subtract(retiros).multiply(porcentajeMontoMaximo).multiply(MESES_MONTO_MAXIMO);
        return redondear(monto.max(BigDecimal.ZERO));
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    private static BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static Respuesta errorBaseDatos(SQLException e) {
        return Respuesta.error("BD-001", "Error de base de datos: " + e.getMessage());
    }
}
