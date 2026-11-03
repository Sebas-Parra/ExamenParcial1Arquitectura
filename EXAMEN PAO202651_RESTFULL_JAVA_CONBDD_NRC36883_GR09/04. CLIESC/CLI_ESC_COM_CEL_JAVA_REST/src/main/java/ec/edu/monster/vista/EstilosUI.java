package ec.edu.monster.vista;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/** Paleta y tratamientos compartidos por los formularios Swing. */
final class EstilosUI {
    static final int ANCHO_VENTANA = 1260;
    static final int ALTO_VENTANA = 740;
    static final int ALTO_CONTROL = 36;
    static final Color FONDO = new Color(241, 244, 248);
    static final Color TINTA = new Color(29, 41, 57);
    static final Color AZUL_MARINO = new Color(24, 50, 87);
    static final Color AZUL_HOVER = new Color(35, 68, 112);
    static final Color SUAVE = new Color(232, 237, 244);
    static final Color GRIS = new Color(91, 105, 122);
    static final Color BORDE = new Color(211, 219, 229);
    static final Color BLANCO = new Color(252, 253, 254);
    static final Color FILA_ALTERNA = new Color(247, 249, 252);

    private EstilosUI() { }

    static void configurarVentana(JFrame ventana) {
        ventana.setSize(new Dimension(ANCHO_VENTANA, ALTO_VENTANA));
        ventana.setMinimumSize(new Dimension(1024, 680));
        ventana.setLocationRelativeTo(null);
    }

    static Border bordeCampo() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(3, 12, 3, 12));
    }

    static void estilizarCampo(JTextField campo) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        campo.setForeground(TINTA);
        campo.setBackground(BLANCO);
        campo.setBorder(bordeCampo());
        campo.setCaretColor(AZUL_MARINO);
        campo.setPreferredSize(new Dimension(200, ALTO_CONTROL));
        campo.setMinimumSize(new Dimension(0, ALTO_CONTROL));
    }

    static void estilizarSelector(JComboBox<?> selector) {
        selector.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        selector.setBackground(BLANCO);
        selector.setForeground(TINTA);
        selector.setPreferredSize(new Dimension(200, ALTO_CONTROL));
        selector.setMinimumSize(new Dimension(0, ALTO_CONTROL));
    }

    static void estilizarBotonPrincipal(JButton boton) {
        boton.setUI(new BasicButtonUI());
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(BLANCO);
        boton.setBackground(AZUL_MARINO);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        Dimension preferred = boton.getPreferredSize();
        boton.setPreferredSize(new Dimension(Math.max(150, preferred.width), ALTO_CONTROL));
        boton.setMinimumSize(new Dimension(120, ALTO_CONTROL));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
        boton.getModel().addChangeListener(evento -> boton.setBackground(
                boton.getModel().isRollover() && boton.isEnabled() ? AZUL_HOVER : AZUL_MARINO));
    }

    static void estilizarBotonSecundario(JButton boton) {
        boton.setUI(new BasicButtonUI());
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setForeground(AZUL_MARINO);
        boton.setBackground(SUAVE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        Dimension preferred = boton.getPreferredSize();
        boton.setPreferredSize(new Dimension(Math.max(130, preferred.width), ALTO_CONTROL));
        boton.setMinimumSize(new Dimension(100, ALTO_CONTROL));
        boton.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }

    static JLabel etiqueta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        label.setForeground(TINTA);
        return label;
    }

    static JLabel ayuda(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(GRIS);
        return label;
    }

    static JLabel tituloTarjeta(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(AZUL_MARINO);
        return label;
    }

    /** Panel blanco con borde, usado como tarjeta de contenido. */
    static JPanel tarjeta(java.awt.LayoutManager layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(BLANCO);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDE),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        return panel;
    }

    /** Tabla de solo lectura con filas alternas y columnas numéricas alineadas a la derecha. */
    static JTable tabla(ModeloTabla<?> modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabla.setForeground(TINTA);
        tabla.setRowHeight(30);
        tabla.setShowVerticalLines(false);
        tabla.setGridColor(BORDE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setSelectionBackground(new Color(214, 226, 242));
        tabla.setSelectionForeground(TINTA);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);
        JTableHeader cabecera = tabla.getTableHeader();
        cabecera.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cabecera.setBackground(SUAVE);
        cabecera.setForeground(AZUL_MARINO);
        cabecera.setPreferredSize(new Dimension(0, 32));
        for (int i = 0; i < modelo.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(new Celda(modelo.alineadaDerecha(i)));
        }
        return tabla;
    }

    static JScrollPane desplazable(Component componente) {
        JScrollPane panel = new JScrollPane(componente);
        panel.setBorder(BorderFactory.createLineBorder(BORDE));
        panel.getViewport().setBackground(BLANCO);
        return panel;
    }

    private static final class Celda extends DefaultTableCellRenderer {
        private final boolean derecha;

        Celda(boolean derecha) {
            this.derecha = derecha;
        }

        @Override
        public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                boolean foco, int fila, int columna) {
            super.getTableCellRendererComponent(tabla, valor, seleccionada, false, fila, columna);
            setHorizontalAlignment(derecha ? SwingConstants.RIGHT : SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            if (!seleccionada) {
                setBackground(fila % 2 == 0 ? BLANCO : FILA_ALTERNA);
                setForeground(TINTA);
            }
            return this;
        }
    }
}
