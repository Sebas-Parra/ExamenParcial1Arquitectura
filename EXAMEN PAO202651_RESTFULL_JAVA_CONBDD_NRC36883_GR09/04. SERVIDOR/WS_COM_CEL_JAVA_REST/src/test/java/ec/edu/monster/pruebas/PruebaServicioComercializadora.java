package ec.edu.monster.pruebas;

import ec.edu.monster.datos.RepositorioComercializadora;
import ec.edu.monster.integracion.BancoNoDisponibleException;
import ec.edu.monster.integracion.ServicioBanco;
import ec.edu.monster.modelos.Celular;
import ec.edu.monster.modelos.CuotaAmortizacion;
import ec.edu.monster.modelos.DetalleFactura;
import ec.edu.monster.modelos.Factura;
import ec.edu.monster.modelos.FacturaRequest;
import ec.edu.monster.modelos.Respuesta;
import ec.edu.monster.servicios.CatalogoServices;
import ec.edu.monster.servicios.FacturacionServices;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pruebas locales del catálogo y la facturación, sin base de datos, sin banco y sin servidor de aplicaciones. */
public class PruebaServicioComercializadora {
    private static final String ANA = "1712345678";
    private static final String LUIS = "1734567890";
    private static final String SOFIA = "1767890123";
    private static int realizadas;

    public static void main(String[] args) {
        probarCatalogo();
        probarValidacionesFactura();
        probarVentaEfectivo();
        probarVentaCredito();
        probarConsultas();
        System.out.println("Pruebas locales de la comercializadora: " + realizadas + " verificaciones OK");
    }

    private static void probarCatalogo() {
        RepositorioMemoria repositorio = new RepositorioMemoria();
        CatalogoServices servicio = new CatalogoServices(repositorio);

        Respuesta lista = servicio.listarCelulares(false);
        codigo(lista, "OK", "listar catálogo");
        comprobar(lista.getCelulares().size() == 3, "el catálogo disponible debe tener 3 celulares");
        comprobar(servicio.listarCelulares(true).getCelulares().size() == 4, "con retirados deben ser 4");

        codigo(servicio.crearCelular(null), "DATA-001", "crear sin datos");
        codigo(servicio.crearCelular(celular(" ", "X", "100")), "DATA-001", "crear sin marca");
        codigo(servicio.crearCelular(celular("Nokia", "G42", "0")), "DATA-001", "crear con precio cero");
        codigo(servicio.crearCelular(celular("Nokia", "G42", "10.555")), "DATA-001", "crear con tres decimales");

        Respuesta creado = servicio.crearCelular(celular(" Nokia ", "G42", "159.9"));
        codigo(creado, "OK", "crear celular");
        comprobar(creado.getCelular().getCodCelular() == 5, "debe devolver el código generado");
        comprobar("Nokia".equals(creado.getCelular().getMarca()), "la marca se guarda sin espacios");
        comprobar(new BigDecimal("159.90").equals(creado.getCelular().getPrecio()), "el precio se guarda con 2 decimales");
        comprobar(Boolean.TRUE.equals(creado.getCelular().getDisponible()), "un celular nuevo está disponible");

        codigo(servicio.actualizarCelular(99, celular("Nokia", "G42", "150")), "CEL-001", "actualizar inexistente");
        Respuesta actualizado = servicio.actualizarCelular(5, celular("Nokia", "G42 5G", "175"));
        codigo(actualizado, "OK", "actualizar celular");
        comprobar("G42 5G".equals(repositorio.celulares.get(5).getModelo()), "debe guardar el nuevo modelo");
        comprobar(Boolean.TRUE.equals(repositorio.celulares.get(5).getDisponible()), "actualizar conserva la disponibilidad");

        codigo(servicio.obtenerCelular(5), "OK", "obtener celular");
        codigo(servicio.obtenerCelular(99), "CEL-001", "obtener inexistente");
        codigo(servicio.eliminarCelular(99), "CEL-001", "eliminar inexistente");
        Respuesta eliminado = servicio.eliminarCelular(5);
        codigo(eliminado, "OK", "eliminar celular");
        comprobar(Boolean.FALSE.equals(repositorio.celulares.get(5).getDisponible()), "eliminar lo retira del catálogo");
        comprobar(servicio.listarCelulares(false).getCelulares().size() == 3, "el retirado ya no se lista");
    }

    private static void probarValidacionesFactura() {
        FacturacionServices servicio = servicio(new RepositorioMemoria(), new BancoMemoria());
        codigo(servicio.facturar(null), "DATA-001", "factura nula");
        codigo(servicio.facturar(efectivo("123", 1, 1)), "DATA-001", "cédula inválida");
        codigo(servicio.facturar(new FacturaRequest(ANA, "Ana", "TARJETA", null, detalle(1, 1))),
            "DATA-001", "forma de pago inválida");
        codigo(servicio.facturar(new FacturaRequest(ANA, " ", "EFECTIVO", null, detalle(1, 1))),
            "DATA-001", "efectivo sin nombre");
        codigo(servicio.facturar(credito(ANA, null, 1)), "DATA-001", "crédito sin plazo");
        codigo(servicio.facturar(credito(ANA, 2, 1)), "DATA-001", "plazo menor a 3 meses");
        codigo(servicio.facturar(credito(ANA, 19, 1)), "DATA-001", "plazo mayor a 18 meses");
        codigo(servicio.facturar(new FacturaRequest(ANA, "Ana", "EFECTIVO", null, List.of())),
            "DATA-001", "factura sin detalle");
        codigo(servicio.facturar(efectivo(ANA, 1, 0)), "DATA-001", "cantidad cero");
        codigo(servicio.facturar(efectivo(ANA, 99, 1)), "CEL-001", "celular inexistente");
        codigo(servicio.facturar(efectivo(ANA, 4, 1)), "CEL-002", "celular retirado del catálogo");
    }

    private static void probarVentaEfectivo() {
        RepositorioMemoria repositorio = new RepositorioMemoria();
        BancoMemoria banco = new BancoMemoria();
        FacturacionServices servicio = servicio(repositorio, banco);

        Respuesta simple = servicio.facturar(new FacturaRequest(ANA, " Ana Lucía Torres ", "efectivo", null, detalle(1, 1)));
        codigo(simple, "OK", "venta en efectivo");
        Factura factura = simple.getFactura();
        comprobar(factura.getCodFactura() == 1, "debe devolver el código de factura");
        comprobar("EFECTIVO".equals(factura.getFormaPago()), "la forma de pago se normaliza a EFECTIVO");
        comprobar("Ana Lucía Torres".equals(factura.getNombre()), "el nombre se guarda sin espacios");
        // 199.00 - 42% = 199.00 - 83.58 = 115.42
        comprobar(new BigDecimal("83.58").equals(factura.getDescuento()), "descuento esperado 83.58: " + factura.getDescuento());
        comprobar(new BigDecimal("115.42").equals(factura.getTotal()), "total esperado 115.42: " + factura.getTotal());
        comprobar(factura.getCodCredito() == null, "en efectivo no hay crédito");
        comprobar(banco.llamadas == 0, "en efectivo no se invoca al banco");

        List<DetalleFactura> detalles = new ArrayList<>(detalle(2, 1));
        detalles.addAll(detalle(3, 1));
        detalles.addAll(detalle(2, 1));
        Respuesta varios = servicio.facturar(new FacturaRequest(ANA, "Ana", "EFECTIVO", null, detalles));
        codigo(varios, "OK", "venta en efectivo de varios celulares");
        // 2 x 450.00 + 249.00 = 1149.00; descuento 482.58; total 666.42
        comprobar(varios.getFactura().getDetalles().size() == 2, "el mismo celular se agrupa en una línea");
        comprobar(varios.getFactura().getDetalles().get(0).getCantidad() == 2, "la cantidad agrupada debe ser 2");
        comprobar(new BigDecimal("1149.00").equals(varios.getFactura().getSubtotal()), "subtotal esperado 1149.00");
        comprobar(new BigDecimal("666.42").equals(varios.getFactura().getTotal()),
            "total esperado 666.42: " + varios.getFactura().getTotal());
        comprobar(repositorio.facturas.size() == 2, "deben quedar 2 facturas registradas");
    }

    private static void probarVentaCredito() {
        RepositorioMemoria repositorio = new RepositorioMemoria();
        BancoMemoria banco = new BancoMemoria();
        FacturacionServices servicio = servicio(repositorio, banco);

        Respuesta noSujeto = servicio.facturar(credito(LUIS, 6, 1));
        codigo(noSujeto, "CRED-001", "comprador que no es sujeto de crédito");
        comprobar(noSujeto.getMotivo().contains("25"), "el rechazo informa el motivo del banco");

        Respuesta noCliente = servicio.facturar(credito("9999999999", 6, 1));
        codigo(noCliente, "CRED-001", "comprador que no es cliente del banco");

        Respuesta caro = servicio.facturar(credito(SOFIA, 6, 3));
        codigo(caro, "CRED-002", "compra mayor al monto máximo");
        comprobar(new BigDecimal("225.00").equals(caro.getMontoMaximo()), "el rechazo informa el monto máximo");
        comprobar(banco.creditos.isEmpty(), "no se debe otorgar crédito si se rechaza");
        comprobar(repositorio.facturas.isEmpty(), "no se debe registrar factura si se rechaza");

        Respuesta aprobado = servicio.facturar(new FacturaRequest(ANA, null, "CREDITO", 6, detalle(2, 1)));
        codigo(aprobado, "OK", "venta con crédito directo");
        Factura factura = aprobado.getFactura();
        comprobar(factura.getCodCredito() == 1, "la factura guarda el crédito otorgado por el banco");
        comprobar(new BigDecimal("450.00").equals(factura.getTotal()), "en crédito no hay descuento");
        comprobar(BigDecimal.ZERO.setScale(2).equals(factura.getDescuento()), "el descuento en crédito es 0");
        comprobar(factura.getNumeroCuotas() == 6, "la factura guarda el número de cuotas");
        comprobar(new BigDecimal("78.65").equals(factura.getValorCuota()), "la factura guarda la cuota del banco");
        comprobar("Ana Lucía Torres".equals(factura.getNombre()), "en crédito el nombre se toma del banco");
        comprobar(aprobado.getTablaAmortizacion().size() == 6, "debe devolver la tabla de amortización");
        comprobar(new BigDecimal("450.00").equals(banco.creditos.get(1)), "el banco recibe el valor de la compra");
        comprobar(repositorio.facturas.size() == 1, "debe registrar la factura");

        Respuesta segundo = servicio.facturar(credito(ANA, 3, 1));
        codigo(segundo, "CRED-001", "segundo crédito con uno activo");

        banco.disponible = false;
        codigo(servicio.facturar(credito(SOFIA, 3, 1)), "BANCO-001", "banco no disponible");
    }

    private static void probarConsultas() {
        RepositorioMemoria repositorio = new RepositorioMemoria();
        BancoMemoria banco = new BancoMemoria();
        FacturacionServices servicio = servicio(repositorio, banco);
        Respuesta aprobado = servicio.facturar(credito(ANA, 6, 2));
        codigo(aprobado, "OK", "crédito para consultas");

        codigo(servicio.obtenerTablaAmortizacion(0), "DATA-001", "código de crédito inválido");
        Respuesta tabla = servicio.obtenerTablaAmortizacion(aprobado.getFactura().getCodCredito());
        codigo(tabla, "OK", "tabla de amortización desde la comercializadora");
        comprobar(tabla.getTablaAmortizacion().size() == 6, "la tabla consultada debe tener 6 cuotas");
        codigo(servicio.obtenerTablaAmortizacion(99), "CRED-003", "crédito inexistente en el banco");

        codigo(servicio.listarCreditos("12"), "DATA-001", "créditos con cédula inválida");
        codigo(servicio.listarCreditos(ANA), "OK", "créditos del comprador");

        codigo(servicio.obtenerFactura(aprobado.getFactura().getCodFactura()), "OK", "obtener factura");
        codigo(servicio.obtenerFactura(99), "FAC-001", "factura inexistente");
        comprobar(servicio.listarFacturas(ANA).getFacturas().size() == 1, "Ana debe tener una factura");
        comprobar(servicio.listarFacturas(null).getFacturas().size() == 1, "sin cédula se listan todas");

        banco.disponible = false;
        codigo(servicio.obtenerTablaAmortizacion(1), "BANCO-001", "tabla con el banco caído");
    }

    private static FacturacionServices servicio(RepositorioComercializadora repositorio, ServicioBanco banco) {
        Clock reloj = Clock.fixed(LocalDateTime.of(2026, 10, 27, 10, 30).atZone(ZoneId.of("UTC")).toInstant(),
            ZoneId.of("UTC"));
        return new FacturacionServices(repositorio, banco, reloj);
    }

    private static Celular celular(String marca, String modelo, String precio) {
        return new Celular(null, marca, modelo, null, new BigDecimal(precio), null);
    }

    private static List<DetalleFactura> detalle(int codCelular, int cantidad) {
        return List.of(new DetalleFactura(codCelular, null, null, cantidad, null, null));
    }

    private static FacturaRequest efectivo(String cedula, int codCelular, int cantidad) {
        return new FacturaRequest(cedula, "Comprador", "EFECTIVO", null, detalle(codCelular, cantidad));
    }

    private static FacturaRequest credito(String cedula, Integer cuotas, int codCelular) {
        return new FacturaRequest(cedula, null, "CREDITO", cuotas, detalle(codCelular, 1));
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

    /** Catálogo y facturas en memoria. */
    private static final class RepositorioMemoria implements RepositorioComercializadora {
        private final Map<Integer, Celular> celulares = new LinkedHashMap<>();
        private final Map<Integer, Factura> facturas = new LinkedHashMap<>();

        RepositorioMemoria() {
            celulares.put(1, new Celular(1, "Samsung", "Galaxy A15", null, new BigDecimal("199.00"), true));
            celulares.put(2, new Celular(2, "Samsung", "Galaxy A55", null, new BigDecimal("450.00"), true));
            celulares.put(3, new Celular(3, "Xiaomi", "Redmi Note 13", null, new BigDecimal("249.00"), true));
            celulares.put(4, new Celular(4, "Nokia", "3310", null, new BigDecimal("50.00"), false));
        }

        @Override
        public List<Celular> listarCelulares(boolean soloDisponibles) {
            return celulares.values().stream().filter(c -> !soloDisponibles || c.getDisponible()).toList();
        }

        @Override
        public Celular buscarCelular(int codCelular) {
            Celular c = celulares.get(codCelular);
            return c == null ? null
                : new Celular(c.getCodCelular(), c.getMarca(), c.getModelo(), c.getDescripcion(), c.getPrecio(), c.getDisponible());
        }

        @Override
        public int crearCelular(Celular celular) {
            int codigo = celulares.size() + 1;
            celulares.put(codigo, new Celular(codigo, celular.getMarca(), celular.getModelo(), celular.getDescripcion(),
                celular.getPrecio(), celular.getDisponible()));
            return codigo;
        }

        @Override
        public void actualizarCelular(Celular celular) {
            celulares.put(celular.getCodCelular(), celular);
        }

        @Override
        public void retirarCelular(int codCelular) {
            celulares.get(codCelular).setDisponible(false);
        }

        @Override
        public int registrarFactura(Factura factura) {
            int codigo = facturas.size() + 1;
            facturas.put(codigo, factura);
            return codigo;
        }

        @Override
        public Factura buscarFactura(int codFactura) {
            return facturas.get(codFactura);
        }

        @Override
        public List<Factura> listarFacturas(String cedula) {
            return facturas.values().stream().filter(f -> cedula == null || cedula.equals(f.getCedula())).toList();
        }
    }

    /** Banco simulado con los escenarios de BANQUITO.sql que usa la comercializadora. */
    private static final class BancoMemoria implements ServicioBanco {
        private final Map<Integer, BigDecimal> creditos = new LinkedHashMap<>();
        private final Map<Integer, Integer> plazos = new LinkedHashMap<>();
        private final Map<Integer, Integer> clienteDelCredito = new LinkedHashMap<>();
        private boolean disponible = true;
        private int llamadas;

        private void llamar() {
            llamadas++;
            if (!disponible) throw new BancoNoDisponibleException("No se pudo conectar con el Banco BanQuito", null);
        }

        @Override
        public Respuesta verificarSujetoCredito(String cedula) {
            llamar();
            Respuesta r = Respuesta.exito("Verificacion realizada");
            r.setCedula(cedula);
            switch (cedula) {
                case ANA -> { cliente(r, 1, "Ana Lucía Torres"); r.setSujetoCredito(!clienteDelCredito.containsValue(1));
                    r.setMotivo(r.getSujetoCredito() ? "Cumple" : "Ya tiene un credito activo en el banco"); }
                case LUIS -> { cliente(r, 3, "Luis Fernando Paredes"); r.setSujetoCredito(false);
                    r.setMotivo("Los clientes de genero masculino deben tener al menos 25 anios"); }
                case SOFIA -> { cliente(r, 6, "Sofía Valentina Vega"); r.setSujetoCredito(true); r.setMotivo("Cumple"); }
                default -> { r.setSujetoCredito(false); r.setMotivo("La persona no es cliente del banco"); }
            }
            return r;
        }

        private static void cliente(Respuesta r, int codCliente, String nombre) {
            r.setCodCliente(codCliente);
            r.setNombre(nombre);
        }

        @Override
        public Respuesta obtenerMontoMaximo(int codCliente) {
            llamar();
            Respuesta r = Respuesta.exito("Monto maximo calculado");
            r.setCodCliente(codCliente);
            r.setMontoMaximo(new BigDecimal(codCliente == 6 ? "225.00" : "1440.00"));
            return r;
        }

        @Override
        public Respuesta otorgarCredito(int codCliente, BigDecimal precio, int numeroCuotas) {
            llamar();
            int codigo = creditos.size() + 1;
            creditos.put(codigo, precio);
            plazos.put(codigo, numeroCuotas);
            clienteDelCredito.put(codigo, codCliente);
            Respuesta r = Respuesta.exito("Credito aprobado");
            r.setCodCredito(codigo);
            r.setTasaAnual(new BigDecimal("16.50"));
            r.setValorCuota(precio.compareTo(new BigDecimal("450.00")) == 0 && numeroCuotas == 6
                ? new BigDecimal("78.65") : precio.divide(BigDecimal.valueOf(numeroCuotas), 2, RoundingMode.HALF_UP));
            r.setTablaAmortizacion(tabla(numeroCuotas, r.getValorCuota()));
            return r;
        }

        @Override
        public Respuesta obtenerTablaAmortizacion(int codCredito) {
            llamar();
            if (!creditos.containsKey(codCredito)) return Respuesta.error("CRED-003", "Credito inexistente");
            Respuesta r = Respuesta.exito("Tabla de amortizacion");
            r.setCodCredito(codCredito);
            r.setTablaAmortizacion(tabla(plazos.get(codCredito), BigDecimal.ONE));
            return r;
        }

        @Override
        public Respuesta listarCreditos(String cedula) {
            llamar();
            return Respuesta.exito("Creditos del cliente");
        }

        private static List<CuotaAmortizacion> tabla(int cuotas, BigDecimal valorCuota) {
            List<CuotaAmortizacion> tabla = new ArrayList<>();
            for (int i = 1; i <= cuotas; i++)
                tabla.add(new CuotaAmortizacion(i, valorCuota, BigDecimal.ZERO, valorCuota, BigDecimal.ZERO));
            return tabla;
        }
    }
}
