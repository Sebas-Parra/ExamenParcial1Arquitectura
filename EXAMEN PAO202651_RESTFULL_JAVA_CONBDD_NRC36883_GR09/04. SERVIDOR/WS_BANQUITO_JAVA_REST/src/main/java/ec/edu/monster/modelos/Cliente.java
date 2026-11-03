package ec.edu.monster.modelos;

import java.time.LocalDate;

/** Cliente del Core bancario (tabla CLIENTE). */
public class Cliente {
    private int codCliente;
    private String cedula;
    private String nombre;
    private String genero;
    private LocalDate fechaNacimiento;

    public Cliente() {
    }

    public Cliente(int codCliente, String cedula, String nombre, String genero, LocalDate fechaNacimiento) {
        this.codCliente = codCliente;
        this.cedula = cedula;
        this.nombre = nombre;
        this.genero = genero;
        this.fechaNacimiento = fechaNacimiento;
    }

    public int getCodCliente() { return codCliente; }
    public void setCodCliente(int codCliente) { this.codCliente = codCliente; }
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}
