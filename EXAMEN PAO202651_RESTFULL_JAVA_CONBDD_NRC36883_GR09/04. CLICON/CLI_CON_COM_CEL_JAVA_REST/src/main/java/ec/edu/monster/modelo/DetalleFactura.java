package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Línea de factura; al facturar solo se envían codCelular y cantidad. */
public final class DetalleFactura {
    private Integer codCelular;
    private String marca;
    private String modelo;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleFactura() {
    }

    public DetalleFactura(Integer codCelular, Integer cantidad) {
        this.codCelular = codCelular;
        this.cantidad = cantidad;
    }

    public Integer getCodCelular() { return codCelular; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}
