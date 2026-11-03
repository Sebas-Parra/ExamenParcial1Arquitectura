package ec.edu.monster.modelo;

import java.util.List;

/** Cuerpo JSON de POST /invoices; Gson omite los campos nulos. */
public final class FacturaRequest {
    private final String cedula;
    private final String nombre;
    private final String formaPago;
    private final Integer numeroCuotas;
    private final List<DetalleFactura> detalles;

    public FacturaRequest(String cedula, String nombre, String formaPago, Integer numeroCuotas,
            List<DetalleFactura> detalles) {
        this.cedula = cedula;
        this.nombre = nombre;
        this.formaPago = formaPago;
        this.numeroCuotas = numeroCuotas;
        this.detalles = detalles;
    }

    public String getCedula() { return cedula; }
    public String getNombre() { return nombre; }
    public String getFormaPago() { return formaPago; }
    public Integer getNumeroCuotas() { return numeroCuotas; }
    public List<DetalleFactura> getDetalles() { return detalles; }
}
