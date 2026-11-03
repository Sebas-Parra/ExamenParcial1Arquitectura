package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Celulares que se van a facturar; calcula los totales que se muestran antes de enviar la factura. */
public final class CarritoVenta {
    public static final String EFECTIVO = "EFECTIVO";
    public static final String CREDITO = "CREDITO";
    public static final BigDecimal DESCUENTO_EFECTIVO = new BigDecimal("0.42");
    public static final int CANTIDAD_MAXIMA = 100;

    /** Línea del carrito con el subtotal ya calculado. */
    public record Linea(Celular celular, int cantidad) {
        public BigDecimal subtotal() {
            return celular.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private final List<Linea> lineas = new ArrayList<>();

    /** Agrega el celular o suma la cantidad si ya estaba en el carrito. */
    public void agregar(Celular celular, int cantidad) {
        Objects.requireNonNull(celular, "celular");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        for (int i = 0; i < lineas.size(); i++) {
            Linea linea = lineas.get(i);
            if (linea.celular().getCodCelular().equals(celular.getCodCelular())) {
                int total = linea.cantidad() + cantidad;
                if (total > CANTIDAD_MAXIMA)
                    throw new IllegalArgumentException("Se admiten hasta " + CANTIDAD_MAXIMA + " unidades por celular.");
                lineas.set(i, new Linea(linea.celular(), total));
                return;
            }
        }
        if (cantidad > CANTIDAD_MAXIMA)
            throw new IllegalArgumentException("Se admiten hasta " + CANTIDAD_MAXIMA + " unidades por celular.");
        lineas.add(new Linea(celular, cantidad));
    }

    public void quitar(int indice) {
        lineas.remove(indice);
    }

    public void vaciar() {
        lineas.clear();
    }

    public boolean estaVacio() {
        return lineas.isEmpty();
    }

    public List<Linea> lineas() {
        return List.copyOf(lineas);
    }

    public BigDecimal subtotal() {
        return lineas.stream().map(Linea::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    /** Descuento del 42% en efectivo; en crédito directo no hay descuento. */
    public BigDecimal descuento(String formaPago) {
        return EFECTIVO.equals(formaPago)
            ? subtotal().multiply(DESCUENTO_EFECTIVO).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2);
    }

    public BigDecimal total(String formaPago) {
        return subtotal().subtract(descuento(formaPago));
    }

    public List<DetalleFactura> detalles() {
        return lineas.stream().map(l -> new DetalleFactura(l.celular().getCodCelular(), l.cantidad())).toList();
    }
}
