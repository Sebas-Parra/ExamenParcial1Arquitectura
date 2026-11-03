package ec.edu.monster.modelos;

import java.math.BigDecimal;

/** Datos JSON para otorgar un crédito. */
public class CreditoRequest {
    private Integer codCliente;
    private BigDecimal precio;
    private Integer numeroCuotas;

    public CreditoRequest() {
    }

    public Integer getCodCliente() { return codCliente; }
    public void setCodCliente(Integer codCliente) { this.codCliente = codCliente; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(Integer numeroCuotas) { this.numeroCuotas = numeroCuotas; }
}
