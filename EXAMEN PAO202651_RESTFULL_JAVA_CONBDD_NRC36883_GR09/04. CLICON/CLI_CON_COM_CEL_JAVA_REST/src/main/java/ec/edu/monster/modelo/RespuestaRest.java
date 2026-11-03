package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.List;

/** Respuesta JSON devuelta por el API REST de la comercializadora. */
public final class RespuestaRest {
    private int estadoHttp;
    private String estado;
    private String codigo;
    private String mensaje;
    private Celular celular;
    private List<Celular> celulares;
    private Factura factura;
    private List<Factura> facturas;
    private String nombre;
    private Boolean sujetoCredito;
    private String motivo;
    private BigDecimal montoMaximo;
    private Integer codCredito;
    private BigDecimal monto;
    private Integer numeroCuotas;
    private BigDecimal tasaAnual;
    private BigDecimal valorCuota;
    private List<CuotaAmortizacion> tablaAmortizacion;
    private List<Credito> creditos;

    public RespuestaRest() {
    }

    public int getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(int estadoHttp) { this.estadoHttp = estadoHttp; }
    public String getEstado() { return estado; }
    public String getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public Celular getCelular() { return celular; }
    public List<Celular> getCelulares() { return celulares == null ? List.of() : celulares; }
    public Factura getFactura() { return factura; }
    public List<Factura> getFacturas() { return facturas == null ? List.of() : facturas; }
    public String getNombre() { return nombre; }
    public Boolean getSujetoCredito() { return sujetoCredito; }
    public String getMotivo() { return motivo; }
    public BigDecimal getMontoMaximo() { return montoMaximo; }
    public Integer getCodCredito() { return codCredito; }
    public BigDecimal getMonto() { return monto; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public BigDecimal getTasaAnual() { return tasaAnual; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public List<CuotaAmortizacion> getTablaAmortizacion() {
        return tablaAmortizacion == null ? List.of() : tablaAmortizacion;
    }
    public List<Credito> getCreditos() { return creditos == null ? List.of() : creditos; }

    public boolean isExito() {
        return estadoHttp >= 200 && estadoHttp < 300 && "OK".equals(codigo);
    }
}
