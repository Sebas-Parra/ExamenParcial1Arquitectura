package ec.edu.monster.modelo;

import java.math.BigDecimal;

/** Teléfono celular del catálogo de la comercializadora. */
public final class Celular {
    private Integer codCelular;
    private String marca;
    private String modelo;
    private String descripcion;
    private BigDecimal precio;
    private Boolean disponible;

    public Celular() {
    }

    public Celular(String marca, String modelo, String descripcion, BigDecimal precio) {
        this.marca = marca;
        this.modelo = modelo;
        this.descripcion = descripcion;
        this.precio = precio;
    }

    public Integer getCodCelular() { return codCelular; }
    public void setCodCelular(Integer codCelular) { this.codCelular = codCelular; }
    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public Boolean getDisponible() { return disponible; }
    public void setDisponible(Boolean disponible) { this.disponible = disponible; }

    public String nombre() {
        return marca + " " + modelo;
    }
}
