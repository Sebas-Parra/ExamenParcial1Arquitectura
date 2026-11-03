package ec.edu.monster.pruebas;

public final class PruebaClienteRest {
    public static void main(String[] args) throws Exception {
        PruebaConsultaCatalogo.ejecutar();
        PruebaMantenimientoCelular.ejecutar();
        PruebaFacturacion.ejecutar();
        PruebaConsultaFacturas.ejecutar();
        PruebaDetalleFactura.ejecutar();
        PruebaTablaAmortizacion.ejecutar();
        PruebaCarritoVenta.ejecutar();
        PruebaControladorFacturacion.ejecutar();
        System.out.println("Pruebas REST del cliente: OK");
    }
}
