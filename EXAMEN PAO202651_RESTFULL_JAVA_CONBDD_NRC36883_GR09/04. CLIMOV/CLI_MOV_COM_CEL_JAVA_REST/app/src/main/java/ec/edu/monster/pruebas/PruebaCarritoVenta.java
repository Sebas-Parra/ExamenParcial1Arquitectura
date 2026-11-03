package ec.edu.monster.pruebas;

import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import java.math.BigDecimal;

final class PruebaCarritoVenta {
    static void ejecutar() {
        Celular a15 = celular(1, "199.00");
        Celular a55 = celular(4, "450.00");
        CarritoVenta carrito = new CarritoVenta();
        SoportePruebasRest.exigir(carrito.estaVacio() && carrito.subtotal().compareTo(BigDecimal.ZERO) == 0,
            "Un carrito nuevo debe estar vacío");

        carrito.agregar(a15, 1);
        SoportePruebasRest.exigir(new BigDecimal("83.58").equals(carrito.descuento(CarritoVenta.EFECTIVO))
            && new BigDecimal("115.42").equals(carrito.total(CarritoVenta.EFECTIVO)),
            "En efectivo 199.00 debe tener 83.58 de descuento y total 115.42");
        SoportePruebasRest.exigir(new BigDecimal("199.00").equals(carrito.total(CarritoVenta.CREDITO))
            && carrito.descuento(CarritoVenta.CREDITO).signum() == 0, "En crédito no hay descuento");

        carrito.agregar(a55, 1);
        carrito.agregar(celular(4, "450.00"), 1);
        SoportePruebasRest.exigir(carrito.lineas().size() == 2 && carrito.lineas().get(1).cantidad() == 2,
            "El mismo celular debe sumarse en una sola línea");
        // 199.00 + 2 x 450.00 = 1099.00; descuento 461.58; total 637.42
        SoportePruebasRest.exigir(new BigDecimal("1099.00").equals(carrito.subtotal())
            && new BigDecimal("637.42").equals(carrito.total(CarritoVenta.EFECTIVO)), "Totales de varias líneas");
        SoportePruebasRest.exigir(carrito.detalles().size() == 2 && carrito.detalles().get(1).getCodCelular() == 4
            && carrito.detalles().get(1).getCantidad() == 2, "Los detalles deben enviar código y cantidad");

        boolean rechazado = false;
        try {
            carrito.agregar(a15, CarritoVenta.CANTIDAD_MAXIMA);
        } catch (IllegalArgumentException e) {
            rechazado = true;
        }
        SoportePruebasRest.exigir(rechazado && carrito.lineas().get(0).cantidad() == 1,
            "No se deben superar las " + CarritoVenta.CANTIDAD_MAXIMA + " unidades por celular");

        carrito.quitar(0);
        SoportePruebasRest.exigir(carrito.lineas().size() == 1 && new BigDecimal("900.00").equals(carrito.subtotal()),
            "Quitar debe eliminar la línea elegida");
        carrito.vaciar();
        SoportePruebasRest.exigir(carrito.estaVacio(), "Vaciar debe dejar el carrito vacío");
    }

    private static Celular celular(int codigo, String precio) {
        Celular celular = new Celular("Samsung", "Modelo " + codigo, null, new BigDecimal(precio));
        celular.setCodCelular(codigo);
        return celular;
    }
}
