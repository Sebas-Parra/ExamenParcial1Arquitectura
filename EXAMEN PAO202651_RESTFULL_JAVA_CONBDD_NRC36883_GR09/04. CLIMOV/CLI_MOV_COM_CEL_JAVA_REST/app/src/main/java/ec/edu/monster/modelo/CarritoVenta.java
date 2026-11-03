package ec.edu.monster.modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Celulares que se van a facturar; calcula los totales que se muestran antes de enviar la factura. */
public final class CarritoVenta {
    public static final String EFECTIVO = "EFECTIVO";
    public static final String CREDITO = "CREDITO";
    public static final BigDecimal DESCUENTO_EFECTIVO = new BigDecimal("0.42");
    public static final int CANTIDAD_MAXIMA = 100;

    /** Línea del carrito con el subtotal ya calculado. */
    public static final class Linea {
        private final Celular celular;
        private final int cantidad;

        Linea(Celular celular, int cantidad) {
            this.celular = celular;
            this.cantidad = cantidad;
        }

        public Celular celular() { return celular; }
        public int cantidad() { return cantidad; }

        public BigDecimal subtotal() {
            return celular.getPrecio().multiply(BigDecimal.valueOf(cantidad));
        }
    }

    private final List<Linea> lineas = new ArrayList<>();

    /** Agrega el celular o suma la cantidad si ya estaba en el carrito. */
    public void agregar(Celular celular, int cantidad) {
        if (celular == null) throw new IllegalArgumentException("Selecciona un celular.");
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
        return Collections.unmodifiableList(new ArrayList<>(lineas));
    }

    public BigDecimal subtotal() {
        BigDecimal suma = BigDecimal.ZERO;
        for (Linea linea : lineas) suma = suma.add(linea.subtotal());
        return suma.setScale(2, RoundingMode.HALF_UP);
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
        List<DetalleFactura> detalles = new ArrayList<>();
        for (Linea linea : lineas)
            detalles.add(new DetalleFactura(linea.celular().getCodCelular(), linea.cantidad()));
        return detalles;
    }
}
