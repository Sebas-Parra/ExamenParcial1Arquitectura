package ec.edu.monster.pruebas;

public final class PruebaClienteRest {
    public static void main(String[] args) throws Exception {
        PruebaConsultaCatalogo.ejecutar();
        PruebaMantenimientoCelular.ejecutar();
        PruebaFacturacion.ejecutar();
        PruebaConsultaFacturas.ejecutar();
        PruebaTablaAmortizacion.ejecutar();
        PruebaFlujoConsola.ejecutar();
        System.out.println("Pruebas REST del cliente: OK");
    }
}
