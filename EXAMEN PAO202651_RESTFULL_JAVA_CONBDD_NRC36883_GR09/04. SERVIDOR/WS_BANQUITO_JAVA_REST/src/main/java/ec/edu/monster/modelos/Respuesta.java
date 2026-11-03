package ec.edu.monster.modelos;

import java.math.BigDecimal;
import java.util.List;

/** Respuesta de negocio serializable como JSON por Jakarta JSON-B; los campos nulos no se envían. */
public class Respuesta {
    private String estado;
    private String codigo;
    private String mensaje;
    private String cedula;
    private Integer codCliente;
    private String nombre;
    private Boolean sujetoCredito;
    private String motivo;
    private BigDecimal promedioDepositos;
    private BigDecimal promedioRetiros;
    private BigDecimal montoMaximo;
    private Integer codCredito;
    private BigDecimal monto;
    private Integer numeroCuotas;
    private BigDecimal tasaAnual;
    private BigDecimal valorCuota;
    private String fecha;
    private List<CuotaAmortizacion> tablaAmortizacion;
    private List<Credito> creditos;

    public Respuesta() {
    }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public Integer getCodCliente() { return codCliente; }
    public void setCodCliente(Integer codCliente) { this.codCliente = codCliente; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Boolean getSujetoCredito() { return sujetoCredito; }
    public void setSujetoCredito(Boolean sujetoCredito) { this.sujetoCredito = sujetoCredito; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public BigDecimal getPromedioDepositos() { return promedioDepositos; }
    public void setPromedioDepositos(BigDecimal promedioDepositos) { this.promedioDepositos = promedioDepositos; }
    public BigDecimal getPromedioRetiros() { return promedioRetiros; }
    public void setPromedioRetiros(BigDecimal promedioRetiros) { this.promedioRetiros = promedioRetiros; }
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
    public BigDecimal getValorCuota() { return valorCuota; }
    public void setValorCuota(BigDecimal valorCuota) { this.valorCuota = valorCuota; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public List<CuotaAmortizacion> getTablaAmortizacion() { return tablaAmortizacion; }
    public void setTablaAmortizacion(List<CuotaAmortizacion> tablaAmortizacion) { this.tablaAmortizacion = tablaAmortizacion; }
    public List<Credito> getCreditos() { return creditos; }
    public void setCreditos(List<Credito> creditos) { this.creditos = creditos; }

    public void copiarCliente(Cliente cliente) {
        cedula = cliente.getCedula();
        codCliente = cliente.getCodCliente();
        nombre = cliente.getNombre();
    }

    public void copiarCredito(Credito credito) {
        codCredito = credito.getCodCredito();
        codCliente = credito.getCodCliente();
        monto = credito.getMonto();
        numeroCuotas = credito.getNumeroCuotas();
        tasaAnual = credito.getTasaAnual();
        valorCuota = credito.getValorCuota();
        fecha = credito.getFecha();
    }

    public static Respuesta exito(String mensaje) {
        Respuesta r = new Respuesta();
        r.setEstado("EXITO");
        r.setCodigo("OK");
        r.setMensaje(mensaje);
        return r;
    }

    public static Respuesta error(String codigo, String mensaje) {
        Respuesta r = new Respuesta();
        r.setEstado("ERROR");
        r.setCodigo(codigo);
        r.setMensaje(mensaje);
        return r;
    }
}
