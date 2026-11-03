package ec.edu.monster.modelos;

import java.math.BigDecimal;
import java.util.List;

/** Factura de venta (tablas FACTURA y DETALLE_FACTURA). Los datos de crédito solo aplican a FORMA_PAGO = CREDITO. */
public class Factura {
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
    public void setCodFactura(Integer codFactura) { this.codFactura = codFactura; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Integer getCodCredito() { return codCredito; }
    public void setCodCredito(Integer codCredito) { this.codCredito = codCredito; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(Integer numeroCuotas) { this.numeroCuotas = numeroCuotas; }
    public BigDecimal getValorCuota() { return valorCuota; }
    public void setValorCuota(BigDecimal valorCuota) { this.valorCuota = valorCuota; }
    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }
}
