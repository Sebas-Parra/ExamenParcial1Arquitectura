package ec.edu.monster.vista;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

/** Modelo de tabla de solo lectura definido por columnas sobre una lista de objetos. */
public final class ModeloTabla<T> extends AbstractTableModel {
    /** Columna con su título, cómo obtener el valor y si es numérica (alineada a la derecha). */
    public record Columna<T>(String titulo, Function<T, Object> valor, boolean derecha) {
        public static <T> Columna<T> texto(String titulo, Function<T, Object> valor) {
            return new Columna<>(titulo, valor, false);
        }

        public static <T> Columna<T> numero(String titulo, Function<T, Object> valor) {
            return new Columna<>(titulo, valor, true);
        }
    }

    private final List<Columna<T>> columnas;
    private List<T> filas = new ArrayList<>();

    @SafeVarargs
    public ModeloTabla(Columna<T>... columnas) {
        this.columnas = List.of(columnas);
    }

    public void setFilas(List<T> filas) {
        this.filas = new ArrayList<>(filas);
        fireTableDataChanged();
    }

    public T fila(int indice) {
        return indice < 0 || indice >= filas.size() ? null : filas.get(indice);
    }

    public List<T> filas() {
        return List.copyOf(filas);
    }

    boolean alineadaDerecha(int columna) {
        return columnas.get(columna).derecha();
    }

    @Override
    public int getRowCount() {
        return filas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.size();
    }

    @Override
    public String getColumnName(int columna) {
        return columnas.get(columna).titulo();
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Object valor = columnas.get(columna).valor().apply(filas.get(fila));
        return valor == null ? "" : valor;
    }
}
