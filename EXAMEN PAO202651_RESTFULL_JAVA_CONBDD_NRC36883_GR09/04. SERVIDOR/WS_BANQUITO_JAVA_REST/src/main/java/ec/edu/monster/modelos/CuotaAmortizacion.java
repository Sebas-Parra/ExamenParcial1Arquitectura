package ec.edu.monster.modelos;

import java.math.BigDecimal;

/** Fila de la tabla de amortización (tabla TABLA_AMORTIZACION). */
public class CuotaAmortizacion {
    private int numeroCuota;
    private BigDecimal valorCuota;
    private BigDecimal interesPagado;
    private BigDecimal capitalPagado;
    private BigDecimal saldo;

    public CuotaAmortizacion() {
    }

    public CuotaAmortizacion(int numeroCuota, BigDecimal valorCuota, BigDecimal interesPagado,
            BigDecimal capitalPagado, BigDecimal saldo) {
        this.numeroCuota = numeroCuota;
        this.valorCuota = valorCuota;
        this.interesPagado = interesPagado;
        this.capitalPagado = capitalPagado;
        this.saldo = saldo;
    }

    public int getNumeroCuota() { return numeroCuota; }
    public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public void setValorCuota(BigDecimal valorCuota) { this.valorCuota = valorCuota; }
    public BigDecimal getInteresPagado() { return interesPagado; }
    public void setInteresPagado(BigDecimal interesPagado) { this.interesPagado = interesPagado; }
    public BigDecimal getCapitalPagado() { return capitalPagado; }
    public void setCapitalPagado(BigDecimal capitalPagado) { this.capitalPagado = capitalPagado; }
    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }
}
