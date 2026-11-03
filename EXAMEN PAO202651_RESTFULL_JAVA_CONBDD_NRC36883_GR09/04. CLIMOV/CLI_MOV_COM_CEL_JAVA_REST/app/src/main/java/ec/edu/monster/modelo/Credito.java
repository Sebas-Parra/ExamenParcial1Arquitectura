package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Crédito otorgado por el Banco BanQuito, tal como lo devuelve su API. */
public class Credito {
    private int codCredito;
    private int codCliente;
    private BigDecimal monto;
    private int numeroCuotas;
    private BigDecimal tasaAnual;
    private BigDecimal valorCuota;
    private String fecha;
    private String estado;

    public Credito() {
    }

    public Credito(int codCredito, int codCliente, BigDecimal monto, int numeroCuotas, BigDecimal tasaAnual,
            BigDecimal valorCuota, String fecha, String estado) {
        this.codCredito = codCredito;
        this.codCliente = codCliente;
        this.monto = monto;
        this.numeroCuotas = numeroCuotas;
        this.tasaAnual = tasaAnual;
        this.valorCuota = valorCuota;
        this.fecha = fecha;
        this.estado = estado;
    }

    public int getCodCredito() { return codCredito; }
    public void setCodCredito(int codCredito) { this.codCredito = codCredito; }
    public int getCodCliente() { return codCliente; }
    public void setCodCliente(int codCliente) { this.codCliente = codCliente; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public int getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(int numeroCuotas) { this.numeroCuotas = numeroCuotas; }
    public BigDecimal getTasaAnual() { return tasaAnual; }
    public void setTasaAnual(BigDecimal tasaAnual) { this.tasaAnual = tasaAnual; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public void setValorCuota(BigDecimal valorCuota) { this.valorCuota = valorCuota; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
