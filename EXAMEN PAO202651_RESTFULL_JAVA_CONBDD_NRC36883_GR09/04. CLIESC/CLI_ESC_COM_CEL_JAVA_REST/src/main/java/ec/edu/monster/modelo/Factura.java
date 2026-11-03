package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.util.List;

/** Factura de venta devuelta por la comercializadora. */
public final class Factura {
    private Integer codFactura;
    private String cedula;
    private String nombre;
    private String fecha;
    private String formaPago;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private Integer codCredito;
    private Integer numeroCuotas;
    private BigDecimal valorCuota;
    private List<DetalleFactura> detalles;

    public Factura() {
    }

    public Integer getCodFactura() { return codFactura; }
    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getFecha() { return fecha; }
    public String getFormaPago() { return formaPago; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescuento() { return descuento; }
    public BigDecimal getTotal() { return total; }
    public Integer getCodCredito() { return codCredito; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public List<DetalleFactura> getDetalles() { return detalles == null ? List.of() : detalles; }
}
