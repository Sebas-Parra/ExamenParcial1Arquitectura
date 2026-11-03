package ec.edu.monster.servicios;

import ec.edu.monster.datos.RepositorioComercializadora;
import ec.edu.monster.datos.RepositorioComercializadoraJdbc;
import ec.edu.monster.integracion.BancoNoDisponibleException;
import ec.edu.monster.integracion.ClienteBanquitoRest;
import ec.edu.monster.integracion.ServicioBanco;
import ec.edu.monster.modelos.Celular;
import ec.edu.monster.modelos.DetalleFactura;
import ec.edu.monster.modelos.Factura;
import ec.edu.monster.modelos.FacturaRequest;
import ec.edu.monster.modelos.Respuesta;
import jakarta.enterprise.context.ApplicationScoped;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Facturación de la Comercializadora: venta en efectivo con 42% de descuento y venta con crédito directo
 * del Banco BanQuito, más la consulta de los créditos y su tabla de amortización en el banco.
 */
@ApplicationScoped
public class FacturacionServices {
    public static final String EFECTIVO = "EFECTIVO";
    public static final String CREDITO = "CREDITO";
    public static final BigDecimal DESCUENTO_EFECTIVO = new BigDecimal("0.42");
    public static final int CUOTAS_MINIMAS = 3;
    public static final int CUOTAS_MAXIMAS = 18;

    private final RepositorioComercializadora repositorio;
    private final ServicioBanco banco;
    private final Clock reloj;

    public FacturacionServices() {
        this(new RepositorioComercializadoraJdbc(), new ClienteBanquitoRest(), Clock.systemDefaultZone());
    }

    public FacturacionServices(RepositorioComercializadora repositorio, ServicioBanco banco, Clock reloj) {
        this.repositorio = Objects.requireNonNull(repositorio);
        this.banco = Objects.requireNonNull(banco);
        this.reloj = Objects.requireNonNull(reloj);
    }

    /** Factura la venta; con crédito directo solo se registra si el banco aprueba y otorga el crédito. */
    public synchronized Respuesta facturar(FacturaRequest solicitud) {
        if (solicitud == null) return Respuesta.error("DATA-001", "Indique los datos de la factura");
        String cedula = solicitud.getCedula() == null ? "" : solicitud.getCedula().trim();
        if (!cedula.matches("\\d{10}"))
            return Respuesta.error("DATA-001", "La cedula debe tener 10 digitos");
        String formaPago = solicitud.getFormaPago() == null ? ""
            : solicitud.getFormaPago().trim().toUpperCase(Locale.ROOT);
        if (!EFECTIVO.equals(formaPago) && !CREDITO.equals(formaPago))
            return Respuesta.error("DATA-001", "La forma de pago debe ser EFECTIVO o CREDITO");
        String nombre = solicitud.getNombre() == null ? "" : solicitud.getNombre().trim();
        if (EFECTIVO.equals(formaPago) && (nombre.isEmpty() || nombre.length() > 100))
            return Respuesta.error("DATA-001", "Indique el nombre del comprador (hasta 100 caracteres)");
        Integer cuotas = solicitud.getNumeroCuotas();
        if (CREDITO.equals(formaPago) && (cuotas == null || cuotas < CUOTAS_MINIMAS || cuotas > CUOTAS_MAXIMAS))
            return Respuesta.error("DATA-001",
                "El plazo debe estar entre " + CUOTAS_MINIMAS + " y " + CUOTAS_MAXIMAS + " meses");
        Map<Integer, Integer> cantidades = agruparDetalles(solicitud.getDetalles());
        if (cantidades == null)
            return Respuesta.error("DATA-001", "Indique al menos un celular con codigo y cantidad mayores que cero");

        try {
            List<DetalleFactura> detalles = new ArrayList<>();
            BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
            for (Map.Entry<Integer, Integer> linea : cantidades.entrySet()) {
                Celular celular = repositorio.buscarCelular(linea.getKey());
                if (celular == null) return Respuesta.error("CEL-001", "Celular " + linea.getKey() + " inexistente");
                if (!Boolean.TRUE.equals(celular.getDisponible()))
                    return Respuesta.error("CEL-002", "El celular " + celular.getMarca() + " " + celular.getModelo()
                        + " no esta disponible para la venta");
                BigDecimal valor = celular.getPrecio().multiply(BigDecimal.valueOf(linea.getValue()));
                detalles.add(new DetalleFactura(celular.getCodCelular(), celular.getMarca(), celular.getModelo(),
                    linea.getValue(), celular.getPrecio(), valor));
                subtotal = subtotal.add(valor);
            }

            Factura factura = new Factura();
            factura.setCedula(cedula);
            factura.setNombre(nombre);
            factura.setFecha(LocalDateTime.now(reloj).truncatedTo(ChronoUnit.SECONDS).toString());
            factura.setFormaPago(formaPago);
            factura.setSubtotal(subtotal);
            factura.setDetalles(detalles);

            if (EFECTIVO.equals(formaPago)) {
                BigDecimal descuento = subtotal.multiply(DESCUENTO_EFECTIVO).setScale(2, RoundingMode.HALF_UP);
                factura.setDescuento(descuento);
                factura.setTotal(subtotal.subtract(descuento));
                factura.setCodFactura(repositorio.registrarFactura(factura));
                Respuesta respuesta = Respuesta.exito("Factura registrada con pago en efectivo");
                respuesta.setFactura(factura);
                return respuesta;
            }
            return facturarCredito(factura, cuotas);
        } catch (SQLException e) {
            return errorBaseDatos(e);
        } catch (BancoNoDisponibleException e) {
            return Respuesta.error("BANCO-001", e.getMessage());
        }
    }

    /** Invoca al banco: sujeto de crédito, monto máximo y otorgamiento del crédito; luego registra la factura. */
    private Respuesta facturarCredito(Factura factura, int cuotas) throws SQLException {
        factura.setDescuento(BigDecimal.ZERO.setScale(2));
        factura.setTotal(factura.getSubtotal());

        Respuesta sujeto = banco.verificarSujetoCredito(factura.getCedula());
        if (!sujeto.esExito()) return errorBanco(sujeto);
        if (!Boolean.TRUE.equals(sujeto.getSujetoCredito())) {
            Respuesta rechazo = Respuesta.error("CRED-001", "El comprador no es sujeto de credito del Banco BanQuito");
            rechazo.setCedula(factura.getCedula());
            rechazo.setNombre(sujeto.getNombre());
            rechazo.setSujetoCredito(false);
            rechazo.setMotivo(sujeto.getMotivo());
            return rechazo;
        }

        Respuesta maximo = banco.obtenerMontoMaximo(sujeto.getCodCliente());
        if (!maximo.esExito()) return errorBanco(maximo);
        if (factura.getTotal().compareTo(maximo.getMontoMaximo()) > 0) {
            Respuesta rechazo = Respuesta.error("CRED-002",
                "El valor de la compra supera el monto maximo de credito aprobado");
            rechazo.setCedula(factura.getCedula());
            rechazo.setNombre(sujeto.getNombre());
            rechazo.setSujetoCredito(true);
            rechazo.setMontoMaximo(maximo.getMontoMaximo());
            return rechazo;
        }

        Respuesta credito = banco.otorgarCredito(sujeto.getCodCliente(), factura.getTotal(), cuotas);
        if (!credito.esExito()) return errorBanco(credito);

        factura.setNombre(sujeto.getNombre());
        factura.setCodCredito(credito.getCodCredito());
        factura.setNumeroCuotas(cuotas);
        factura.setValorCuota(credito.getValorCuota());
        try {
            factura.setCodFactura(repositorio.registrarFactura(factura));
        } catch (SQLException e) {
            return Respuesta.error("BD-001", "El banco otorgo el credito " + credito.getCodCredito()
                + " pero no se pudo registrar la factura: " + e.getMessage());
        }

        Respuesta respuesta = Respuesta.exito("Factura registrada con credito directo");
        respuesta.setFactura(factura);
        respuesta.setCodCliente(sujeto.getCodCliente());
        respuesta.setMontoMaximo(maximo.getMontoMaximo());
        respuesta.setCodCredito(credito.getCodCredito());
        respuesta.setMonto(factura.getTotal());
        respuesta.setNumeroCuotas(cuotas);
        respuesta.setTasaAnual(credito.getTasaAnual());
        respuesta.setTablaAmortizacion(credito.getTablaAmortizacion());
        return respuesta;
    }

    public Respuesta obtenerFactura(Integer codFactura) {
        if (codFactura == null || codFactura <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de factura valido");
        try {
            Factura factura = repositorio.buscarFactura(codFactura);
            if (factura == null) return Respuesta.error("FAC-001", "Factura inexistente");
            Respuesta respuesta = Respuesta.exito("Factura encontrada");
            respuesta.setFactura(factura);
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Lista las facturas; si la cédula viene vacía se listan todas. */
    public Respuesta listarFacturas(String cedula) {
        try {
            Respuesta respuesta = Respuesta.exito("Facturas");
            respuesta.setFacturas(repositorio.listarFacturas(cedula == null || cedula.isBlank() ? null : cedula.trim()));
            return respuesta;
        } catch (SQLException e) {
            return errorBaseDatos(e);
        }
    }

    /** Consulta en el banco los créditos de un comprador, para elegir cuál tabla de amortización ver. */
    public Respuesta listarCreditos(String cedula) {
        if (cedula == null || !cedula.trim().matches("\\d{10}"))
            return Respuesta.error("DATA-001", "La cedula debe tener 10 digitos");
        try {
            return banco.listarCreditos(cedula.trim());
        } catch (BancoNoDisponibleException e) {
            return Respuesta.error("BANCO-001", e.getMessage());
        }
    }

    /** Consulta en el banco la tabla de amortización de un crédito otorgado. */
    public Respuesta obtenerTablaAmortizacion(Integer codCredito) {
        if (codCredito == null || codCredito <= 0)
            return Respuesta.error("DATA-001", "Indique un codigo de credito valido");
        try {
            return banco.obtenerTablaAmortizacion(codCredito);
        } catch (BancoNoDisponibleException e) {
            return Respuesta.error("BANCO-001", e.getMessage());
        }
    }

    /** Suma las cantidades de un mismo celular; null si el detalle está vacío o tiene datos inválidos. */
    private static Map<Integer, Integer> agruparDetalles(List<DetalleFactura> detalles) {
        if (detalles == null || detalles.isEmpty()) return null;
        Map<Integer, Integer> cantidades = new LinkedHashMap<>();
        for (DetalleFactura detalle : detalles) {
            if (detalle == null || detalle.getCodCelular() == null || detalle.getCodCelular() <= 0
                    || detalle.getCantidad() == null || detalle.getCantidad() <= 0 || detalle.getCantidad() > 100)
                return null;
            cantidades.merge(detalle.getCodCelular(), detalle.getCantidad(), Integer::sum);
        }
        return cantidades;
    }

    private static Respuesta errorBanco(Respuesta banco) {
        Respuesta error = Respuesta.error(banco.getCodigo(), banco.getMensaje());
        error.setSujetoCredito(banco.getSujetoCredito());
        error.setMotivo(banco.getMotivo());
        error.setMontoMaximo(banco.getMontoMaximo());
        return error;
    }

    private static Respuesta errorBaseDatos(SQLException e) {
        return Respuesta.error("BD-001", "Error de base de datos: " + e.getMessage());
    }
}
