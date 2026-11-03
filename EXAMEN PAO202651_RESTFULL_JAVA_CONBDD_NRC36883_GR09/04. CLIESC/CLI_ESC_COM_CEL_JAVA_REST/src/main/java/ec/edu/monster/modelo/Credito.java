package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Crédito directo otorgado por el Banco BanQuito. */
public final class Credito {
    private int codCredito;
    private BigDecimal monto;
    private int numeroCuotas;
    private BigDecimal tasaAnual;
    private BigDecimal valorCuota;
    private String fecha;
    private String estado;

    public Credito() {
    }

    public int getCodCredito() { return codCredito; }
    public BigDecimal getMonto() { return monto; }
    public int getNumeroCuotas() { return numeroCuotas; }
    public BigDecimal getTasaAnual() { return tasaAnual; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public String getFecha() { return fecha; }
    public String getEstado() { return estado; }
}
