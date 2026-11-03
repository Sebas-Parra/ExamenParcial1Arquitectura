package ec.edu.monster.pruebas;

import ec.edu.monster.datos.RepositorioBanquito;
import ec.edu.monster.modelos.Cliente;
import ec.edu.monster.modelos.Credito;
import ec.edu.monster.modelos.CuotaAmortizacion;
import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.CreditoServices;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Pruebas locales de las reglas de crédito, sin base de datos ni servidor de aplicaciones. */
public class PruebaServicioCredito {
    private static final LocalDate HOY = LocalDate.of(2026, 10, 27);
    private static int realizadas;

    public static void main(String[] args) {
        probarTablaDelEnunciado();
        probarSujetoCredito();
        probarMontoMaximo();
        probarOtorgarCredito();
        probarConsultas();
        System.out.println("Pruebas locales del servicio de credito: " + realizadas + " verificaciones OK");
    }

    private static void probarTablaDelEnunciado() {
        List<CuotaAmortizacion> tabla = CreditoServices.calcularTablaAmortizacion(
            new BigDecimal("450"), 6, new BigDecimal("16.50"));
        String[][] esperado = {
            {"78.65", "6.19", "72.46", "377.54"}, {"78.65", "5.19", "73.46", "304.08"},
            {"78.65", "4.18", "74.47", "229.61"}, {"78.65", "3.16", "75.49", "154.12"},
            {"78.65", "2.12", "76.53", "77.59"}, {"78.65", "1.07", "77.58", "0.00"}};
        comprobar(tabla.size() == 6, "la tabla debe tener 6 cuotas");
        for (int i = 0; i < esperado.length; i++) {
            CuotaAmortizacion c = tabla.get(i);
            String[] real = {c.getValorCuota().toPlainString(), c.getInteresPagado().toPlainString(),
                c.getCapitalPagado().toPlainString(), c.getSaldo().toPlainString()};
            comprobar(java.util.Arrays.equals(esperado[i], real), "cuota " + (i + 1) + ": esperado "
                + String.join(" ", esperado[i]) + " y se obtuvo " + String.join(" ", real));
        }
    }

    private static void probarSujetoCredito() {
        CreditoServices servicio = servicio(new RepositorioMemoria());
        codigo(servicio.verificarSujetoCredito(" "), "DATA-001", "cédula vacía");

        Respuesta noCliente = servicio.verificarSujetoCredito("9999999999");
        codigo(noCliente, "OK", "persona que no es cliente");
        comprobar(Boolean.FALSE.equals(noCliente.getSujetoCredito()), "quien no es cliente no es sujeto de crédito");

        Respuesta ana = servicio.verificarSujetoCredito("1712345678");
        codigo(ana, "OK", "cliente que cumple las reglas");
        comprobar(Boolean.TRUE.equals(ana.getSujetoCredito()), "Ana debe ser sujeto de crédito");
        comprobar(ana.getCodCliente() == 1, "debe devolver el código de cliente");

        rechazado(servicio, "1745678901", "depositos en el ultimo mes");
        rechazado(servicio, "1734567890", "al menos 25");
        rechazado(servicio, "1756789012", "credito activo");
    }

    private static void probarMontoMaximo() {
        CreditoServices servicio = servicio(new RepositorioMemoria());
        codigo(servicio.obtenerMontoMaximo(null), "DATA-001", "código de cliente nulo");
        codigo(servicio.obtenerMontoMaximo(99), "CLI-001", "cliente inexistente");
        Respuesta ana = servicio.obtenerMontoMaximo(1);
        codigo(ana, "OK", "monto máximo de Ana");
        // ((1000 - 200) * 0.30) * 6 = 1440.00; el depósito de hace 4 meses no cuenta.
        comprobar(new BigDecimal("1440.00").equals(ana.getMontoMaximo()),
            "monto máximo esperado 1440.00 y se obtuvo " + ana.getMontoMaximo());
        Respuesta sinAhorro = servicio.obtenerMontoMaximo(6);
        comprobar(BigDecimal.ZERO.setScale(2).equals(sinAhorro.getMontoMaximo()),
            "si retira más de lo que deposita el monto máximo es 0");
    }

    private static void probarOtorgarCredito() {
        RepositorioMemoria repositorio = new RepositorioMemoria();
        CreditoServices servicio = servicio(repositorio);
        codigo(servicio.otorgarCredito(1, new BigDecimal("450"), 2), "DATA-001", "plazo menor a 3 meses");
        codigo(servicio.otorgarCredito(1, new BigDecimal("450"), 19), "DATA-001", "plazo mayor a 18 meses");
        codigo(servicio.otorgarCredito(1, BigDecimal.ZERO, 6), "DATA-001", "precio cero");
        codigo(servicio.otorgarCredito(99, new BigDecimal("450"), 6), "CLI-001", "cliente inexistente");
        codigo(servicio.otorgarCredito(3, new BigDecimal("450"), 6), "CRED-001", "cliente que no es sujeto");
        Respuesta caro = servicio.otorgarCredito(1, new BigDecimal("1440.01"), 6);
        codigo(caro, "CRED-002", "precio mayor al monto máximo");
        comprobar(new BigDecimal("1440.00").equals(caro.getMontoMaximo()), "el rechazo informa el monto máximo");

        Respuesta aprobado = servicio.otorgarCredito(1, new BigDecimal("450"), 6);
        codigo(aprobado, "OK", "crédito aprobado");
        comprobar(aprobado.getCodCredito() != null && aprobado.getCodCredito() > 0, "debe devolver el código del crédito");
        comprobar(new BigDecimal("78.65").equals(aprobado.getValorCuota()), "la cuota debe ser 78.65");
        comprobar(aprobado.getTablaAmortizacion().size() == 6, "debe devolver la tabla de 6 cuotas");
        comprobar(repositorio.tablas.get(aprobado.getCodCredito()).size() == 6, "debe guardar la tabla");

        codigo(servicio.otorgarCredito(1, new BigDecimal("100"), 3), "CRED-001", "segundo crédito con uno activo");
    }

    private static void probarConsultas() {
        RepositorioMemoria repositorio = new RepositorioMemoria();
        CreditoServices servicio = servicio(repositorio);
        Respuesta aprobado = servicio.otorgarCredito(2, new BigDecimal("300.50"), 12);
        codigo(aprobado, "OK", "crédito de Carlos");

        Respuesta tabla = servicio.obtenerTablaAmortizacion(aprobado.getCodCredito());
        codigo(tabla, "OK", "consulta de tabla de amortización");
        comprobar(tabla.getTablaAmortizacion().size() == 12, "la tabla consultada debe tener 12 cuotas");
        comprobar("Carlos".equals(tabla.getNombre()), "la tabla debe incluir el nombre del cliente");
        codigo(servicio.obtenerTablaAmortizacion(999), "CRED-003", "crédito inexistente");

        Respuesta creditos = servicio.listarCreditos("1723456789");
        codigo(creditos, "OK", "créditos de Carlos");
        comprobar(creditos.getCreditos().size() == 1, "Carlos debe tener un crédito");
        codigo(servicio.listarCreditos("0000000000"), "CLI-001", "créditos de un cliente inexistente");
    }

    private static void rechazado(CreditoServices servicio, String cedula, String motivo) {
        Respuesta respuesta = servicio.verificarSujetoCredito(cedula);
        codigo(respuesta, "OK", "verificación de " + cedula);
        comprobar(Boolean.FALSE.equals(respuesta.getSujetoCredito()), cedula + " no debe ser sujeto de crédito");
        comprobar(respuesta.getMotivo().contains(motivo), cedula + ": motivo esperado '" + motivo
            + "' y se obtuvo '" + respuesta.getMotivo() + "'");
    }

    private static CreditoServices servicio(RepositorioBanquito repositorio) {
        Clock reloj = Clock.fixed(HOY.atStartOfDay(ZoneId.of("UTC")).toInstant(), ZoneId.of("UTC"));
        return new CreditoServices(repositorio, reloj, new BigDecimal("0.30"));
    }

    private static void codigo(Respuesta respuesta, String esperado, String operacion) {
        comprobar(respuesta != null, operacion + ": respuesta nula");
        comprobar(esperado.equals(respuesta.getCodigo()), operacion + ": se esperaba " + esperado
            + " y se recibió " + respuesta.getCodigo() + " (" + respuesta.getMensaje() + ")");
        comprobar(("OK".equals(esperado) ? "EXITO" : "ERROR").equals(respuesta.getEstado()),
            operacion + ": estado incorrecto " + respuesta.getEstado());
    }

    private static void comprobar(boolean condicion, String mensaje) {
        realizadas++;
        if (!condicion) throw new AssertionError(mensaje);
    }

    /** Datos equivalentes a los escenarios de BANQUITO.sql, guardados en memoria. */
    private static final class RepositorioMemoria implements RepositorioBanquito {
        private record Movimiento(int codCliente, String tipo, BigDecimal valor, LocalDate fecha) {
        }

        private final List<Cliente> clientes = List.of(
            new Cliente(1, "1712345678", "Ana", "F", LocalDate.of(1990, 3, 15)),
            new Cliente(2, "1723456789", "Carlos", "M", LocalDate.of(1985, 7, 22)),
            new Cliente(3, "1734567890", "Luis", "M", LocalDate.of(2003, 5, 10)),
            new Cliente(4, "1745678901", "María", "F", LocalDate.of(1980, 11, 2)),
            new Cliente(5, "1756789012", "Jorge", "M", LocalDate.of(1975, 1, 28)),
            new Cliente(6, "1767890123", "Sofía", "F", LocalDate.of(1998, 9, 5)));
        private final List<Movimiento> movimientos = new ArrayList<>();
        private final Map<Integer, Credito> creditos = new HashMap<>();
        private final Map<Integer, List<CuotaAmortizacion>> tablas = new HashMap<>();

        RepositorioMemoria() {
            movimiento(1, "DEP", "1200", 5);
            movimiento(1, "DEP", "800", 40);
            movimiento(1, "RET", "200", 10);
            movimiento(1, "DEP", "9000", 120);
            movimiento(2, "DEP", "900", 3);
            movimiento(2, "RET", "100", 20);
            movimiento(3, "DEP", "700", 4);
            movimiento(4, "DEP", "1000", 40);
            movimiento(5, "DEP", "1500", 2);
            movimiento(6, "DEP", "100", 6);
            movimiento(6, "RET", "300", 9);
            creditos.put(1, new Credito(1, 5, new BigDecimal("600.00"), 12, CreditoServices.TASA_ANUAL,
                new BigDecimal("54.58"), HOY.minusDays(20).toString(), "ACTIVO"));
        }

        private void movimiento(int codCliente, String tipo, String valor, int diasAtras) {
            movimientos.add(new Movimiento(codCliente, tipo, new BigDecimal(valor), HOY.minusDays(diasAtras)));
        }

        @Override
        public Cliente buscarClientePorCedula(String cedula) {
            return clientes.stream().filter(c -> c.getCedula().equals(cedula)).findFirst().orElse(null);
        }

        @Override
        public Cliente buscarClientePorCodigo(int codCliente) {
            return clientes.stream().filter(c -> c.getCodCliente() == codCliente).findFirst().orElse(null);
        }

        @Override
        public boolean tieneDepositoDesde(int codCliente, LocalDate desde) {
            return movimientos.stream().anyMatch(m -> m.codCliente() == codCliente && m.tipo().equals("DEP")
                && !m.fecha().isBefore(desde));
        }

        @Override
        public BigDecimal promedioMovimientos(int codCliente, String tipo, LocalDate desde) {
            List<BigDecimal> valores = movimientos.stream().filter(m -> m.codCliente() == codCliente
                && m.tipo().equals(tipo) && !m.fecha().isBefore(desde)).map(Movimiento::valor).toList();
            if (valores.isEmpty()) return BigDecimal.ZERO;
            return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(valores.size()), 6, RoundingMode.HALF_UP);
        }

        @Override
        public boolean tieneCreditoActivo(int codCliente) {
            return creditos.values().stream().anyMatch(c -> c.getCodCliente() == codCliente
                && "ACTIVO".equals(c.getEstado()));
        }

        @Override
        public int registrarCredito(Credito credito, List<CuotaAmortizacion> tabla) {
            int codigo = creditos.size() + 1;
            credito.setCodCredito(codigo);
            creditos.put(codigo, credito);
            tablas.put(codigo, tabla);
            return codigo;
        }

        @Override
        public Credito buscarCredito(int codCredito) {
            return creditos.get(codCredito);
        }

        @Override
        public List<CuotaAmortizacion> obtenerTablaAmortizacion(int codCredito) {
            return tablas.getOrDefault(codCredito, List.of());
        }

        @Override
        public List<Credito> listarCreditos(int codCliente) {
            return creditos.values().stream().filter(c -> c.getCodCliente() == codCliente).toList();
        }
    }
}
