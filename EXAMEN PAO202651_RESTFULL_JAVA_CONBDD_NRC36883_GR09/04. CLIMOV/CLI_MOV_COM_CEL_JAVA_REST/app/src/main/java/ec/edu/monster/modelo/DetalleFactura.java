package ec.edu.monster.modelo;

import java.math.BigDecimal;

/**
 * Línea de una factura. En la solicitud de facturación solo se envían codCelular y cantidad;
 * el resto lo completa el servidor con los datos del catálogo.
 */
public class DetalleFactura {
    private Integer codCelular;
    private String marca;
    private String modelo;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleFactura() {
    }

    public DetalleFactura(Integer codCelular, Integer cantidad) {
        this(codCelular, null, null, cantidad, null, null);
    }

    public DetalleFactura(Integer codCelular, String marca, String modelo, Integer cantidad,
            BigDecimal precioUnitario, BigDecimal subtotal) {
        this.codCelular = codCelular;
        this.marca = marca;
        this.modelo = modelo;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public Integer getCodCelular() { return codCelular; }
    public void setCodCelular(Integer codCelular) { this.codCelular = codCelular; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
