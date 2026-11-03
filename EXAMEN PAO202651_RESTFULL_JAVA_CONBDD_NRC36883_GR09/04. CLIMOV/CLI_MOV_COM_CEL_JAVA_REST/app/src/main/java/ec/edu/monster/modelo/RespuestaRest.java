package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Respuesta JSON del API REST de la comercializadora; ClienteRest llena solo los campos que llegan. */
public final class RespuestaRest {
    private int estadoHttp;
    private String estado;
    private String codigo;
    private String mensaje;
    private Celular celular;
    private List<Celular> celulares = new ArrayList<>();
    private Factura factura;
    private List<Factura> facturas = new ArrayList<>();
    private String nombre;
    private String motivo;
    private BigDecimal montoMaximo;
    private Integer codCredito;
    private BigDecimal monto;
    private Integer numeroCuotas;
    private BigDecimal tasaAnual;
    private List<CuotaAmortizacion> tablaAmortizacion = new ArrayList<>();
    private List<Credito> creditos = new ArrayList<>();

    public int getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(int estadoHttp) { this.estadoHttp = estadoHttp; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public Celular getCelular() { return celular; }
    public void setCelular(Celular celular) { this.celular = celular; }
    public List<Celular> getCelulares() { return celulares; }
    public void setCelulares(List<Celular> celulares) { this.celulares = celulares; }
    public Factura getFactura() { return factura; }
    public void setFactura(Factura factura) { this.factura = factura; }
    public List<Factura> getFacturas() { return facturas; }
    public void setFacturas(List<Factura> facturas) { this.facturas = facturas; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public BigDecimal getMontoMaximo() { return montoMaximo; }
    public void setMontoMaximo(BigDecimal montoMaximo) { this.montoMaximo = montoMaximo; }
    public Integer getCodCredito() { return codCredito; }
    public void setCodCredito(Integer codCredito) { this.codCredito = codCredito; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(Integer numeroCuotas) { this.numeroCuotas = numeroCuotas; }
    public BigDecimal getTasaAnual() { return tasaAnual; }
    public void setTasaAnual(BigDecimal tasaAnual) { this.tasaAnual = tasaAnual; }
    public List<CuotaAmortizacion> getTablaAmortizacion() { return tablaAmortizacion; }
    public void setTablaAmortizacion(List<CuotaAmortizacion> tabla) { this.tablaAmortizacion = tabla; }
    public List<Credito> getCreditos() { return creditos; }
    public void setCreditos(List<Credito> creditos) { this.creditos = creditos; }

    public boolean isExito() {
        return estadoHttp >= 200 && estadoHttp < 300 && "OK".equals(codigo);
    }
}
