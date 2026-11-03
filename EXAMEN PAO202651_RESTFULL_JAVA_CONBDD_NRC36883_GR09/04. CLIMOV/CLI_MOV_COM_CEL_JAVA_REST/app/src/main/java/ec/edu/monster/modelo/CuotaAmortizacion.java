package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Fila de la tabla de amortización devuelta por el Banco BanQuito. */
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

    /**
     * Devuelve la tabla con la fila 0 del formato del enunciado al inicio: solo lleva el saldo
     * inicial, que es el monto del crédito. Si no se conoce el monto se reconstruye con el saldo
     * de la cuota 1 más el capital que esa cuota paga (puede diferir en un centavo por redondeo).
     */
    public static List<CuotaAmortizacion> conFilaInicial(List<CuotaAmortizacion> tabla, BigDecimal monto) {
        if (tabla == null || tabla.isEmpty()) return tabla;
        CuotaAmortizacion primera = tabla.get(0);
        if (primera.getNumeroCuota() == 0) return tabla;
        if (monto == null && (primera.getSaldo() == null || primera.getCapitalPagado() == null)) return tabla;
        CuotaAmortizacion inicial = new CuotaAmortizacion();
        inicial.saldo = monto != null ? monto : primera.getSaldo().add(primera.getCapitalPagado());
        List<CuotaAmortizacion> filas = new ArrayList<>(tabla.size() + 1);
        filas.add(inicial);
        filas.addAll(tabla);
        return filas;
    }
}
