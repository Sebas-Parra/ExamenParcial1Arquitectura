package ec.edu.monster.modelos;

import java.util.List;

/**
 * Cuerpo JSON de POST /api/invoices. El nombre es obligatorio solo en efectivo (en crédito se toma del banco)
 * y numeroCuotas solo en crédito directo.
 */
public class FacturaRequest {
    private String cedula;
    private String nombre;
    private String formaPago;
    private Integer numeroCuotas;
    private List<DetalleFactura> detalles;

    public FacturaRequest() {
    }

    public FacturaRequest(String cedula, String nombre, String formaPago, Integer numeroCuotas,
            List<DetalleFactura> detalles) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.formaPago = formaPago;
        this.numeroCuotas = numeroCuotas;
        this.detalles = detalles;
    }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getFormaPago() { return formaPago; }
    public void setFormaPago(String formaPago) { this.formaPago = formaPago; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public void setNumeroCuotas(Integer numeroCuotas) { this.numeroCuotas = numeroCuotas; }
    public List<DetalleFactura> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleFactura> detalles) { this.detalles = detalles; }
}
