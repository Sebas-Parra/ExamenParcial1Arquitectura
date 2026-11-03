package ec.edu.monster.vista;

import ec.edu.monster.modelo.CarritoVenta;
import ec.edu.monster.modelo.Celular;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/** Formulario de venta: carrito de celulares, forma de pago, datos del comprador y totales. */
public final class PanelFacturacion extends JPanel {
    private final JComboBox<Celular> celulares = new JComboBox<>();
    private final JSpinner cantidad = new JSpinner(new SpinnerNumberModel(1, 1, CarritoVenta.CANTIDAD_MAXIMA, 1));
    private final JButton agregar = new JButton("Agregar");
    private final JButton quitar = new JButton("Quitar");
    private final JButton vaciar = new JButton("Vaciar");
    private final ModeloTabla<CarritoVenta.Linea> modeloCarrito = new ModeloTabla<>(
        ModeloTabla.Columna.texto("Celular", l -> l.celular().nombre()),
        ModeloTabla.Columna.numero("Cantidad", CarritoVenta.Linea::cantidad),
        ModeloTabla.Columna.numero("P. unitario", l -> Formato.dinero(l.celular().getPrecio())),
        ModeloTabla.Columna.numero("Subtotal", l -> Formato.dinero(l.subtotal())));
    private final JTable carrito = EstilosUI.tabla(modeloCarrito);
    private final JRadioButton efectivo = new JRadioButton("Efectivo (42% de descuento)", true);
    private final JRadioButton credito = new JRadioButton("Crédito directo BanQuito");
    private final JTextField cedula = new JTextField();
    private final JTextField nombre = new JTextField();
    private final JLabel etiquetaNombre = EstilosUI.etiqueta("Nombre del comprador");
    private final JLabel etiquetaCuotas = EstilosUI.etiqueta("Número de cuotas mensuales (3 a 18)");
    private final JSpinner cuotas = new JSpinner(new SpinnerNumberModel(6, 3, 18, 1));
    private final JLabel subtotal = valor();
    private final JLabel descuento = valor();
    private final JLabel total = valor();
    private final JLabel notaPago = EstilosUI.ayuda(" ");
    private final JButton facturar = new JButton("Facturar");

    PanelFacturacion() {
        super(new BorderLayout(18, 0));
        setOpaque(false);
        add(crearCarrito(), BorderLayout.CENTER);
        add(crearPago(), BorderLayout.EAST);
        alCambiarFormaPago(e -> actualizarCamposPago());
        actualizarCamposPago();
    }

    private JPanel crearCarrito() {
        JPanel tarjeta = EstilosUI.tarjeta(new BorderLayout(0, 12));
        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.setOpaque(false);
        arriba.add(EstilosUI.tituloTarjeta("1. Celulares a vender"), BorderLayout.NORTH);

        JPanel seleccion = new JPanel(new GridBagLayout());
        seleccion.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 10);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        EstilosUI.estilizarSelector(celulares);
        celulares.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> lista, Object valor, int indice,
                    boolean seleccionado, boolean foco) {
                super.getListCellRendererComponent(lista, valor, indice, seleccionado, foco);
                if (valor instanceof Celular celular)
                    setText(celular.nombre() + "  ·  " + Formato.dinero(celular.getPrecio()));
                return this;
            }
        });
        seleccion.add(celulares, c);
        c.weightx = 0;
        cantidad.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cantidad.setPreferredSize(new Dimension(80, EstilosUI.ALTO_CONTROL));
        seleccion.add(cantidad, c);
        c.insets = new Insets(0, 0, 0, 0);
        EstilosUI.estilizarBotonPrincipal(agregar);
        agregar.setPreferredSize(new Dimension(120, EstilosUI.ALTO_CONTROL));
        seleccion.add(agregar, c);
        arriba.add(seleccion, BorderLayout.CENTER);
        tarjeta.add(arriba, BorderLayout.NORTH);

        int[] anchos = {220, 80, 110, 110};
        for (int i = 0; i < anchos.length; i++) carrito.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tarjeta.add(EstilosUI.desplazable(carrito), BorderLayout.CENTER);

        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.setOpaque(false);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        EstilosUI.estilizarBotonSecundario(quitar);
        EstilosUI.estilizarBotonSecundario(vaciar);
        quitar.setPreferredSize(new Dimension(100, EstilosUI.ALTO_CONTROL));
        vaciar.setPreferredSize(new Dimension(100, EstilosUI.ALTO_CONTROL));
        acciones.add(quitar);
        acciones.add(javax.swing.Box.createHorizontalStrut(10));
        acciones.add(vaciar);
        pie.add(acciones, BorderLayout.WEST);
        pie.add(crearResumen(), BorderLayout.EAST);
        tarjeta.add(pie, BorderLayout.SOUTH);
        return tarjeta;
    }

    private JPanel crearResumen() {
        JPanel resumen = new JPanel(new GridLayout(0, 2, 16, 4));
        resumen.setBackground(EstilosUI.FILA_ALTERNA);
        resumen.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EstilosUI.BORDE), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        resumen.setPreferredSize(new Dimension(270, 104));
        resumen.add(EstilosUI.ayuda("Subtotal"));
        resumen.add(subtotal);
        resumen.add(EstilosUI.ayuda("Descuento"));
        resumen.add(descuento);
        JLabel etiquetaTotal = EstilosUI.etiqueta("TOTAL");
        etiquetaTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        resumen.add(etiquetaTotal);
        total.setFont(new Font("Segoe UI", Font.BOLD, 18));
        total.setForeground(EstilosUI.AZUL_MARINO);
        resumen.add(total);
        return resumen;
    }

    private JPanel crearPago() {
        JPanel tarjeta = EstilosUI.tarjeta(new GridBagLayout());
        tarjeta.setPreferredSize(new Dimension(320, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(0, 0, 8, 0);
        int fila = 0;

        c.gridy = fila++;
        tarjeta.add(EstilosUI.tituloTarjeta("2. Pago y comprador"), c);
        ButtonGroup grupo = new ButtonGroup();
        for (JRadioButton opcion : List.of(efectivo, credito)) {
            opcion.setOpaque(false);
            opcion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            opcion.setForeground(EstilosUI.TINTA);
            grupo.add(opcion);
            c.gridy = fila++;
            c.insets = new Insets(0, 0, 2, 0);
            tarjeta.add(opcion, c);
        }
        c.insets = new Insets(8, 0, 4, 0);
        c.gridy = fila++;
        tarjeta.add(EstilosUI.etiqueta("Cédula del comprador"), c);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = fila++;
        EstilosUI.estilizarCampo(cedula);
        tarjeta.add(cedula, c);
        c.insets = new Insets(8, 0, 4, 0);
        c.gridy = fila++;
        tarjeta.add(etiquetaNombre, c);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = fila++;
        EstilosUI.estilizarCampo(nombre);
        tarjeta.add(nombre, c);
        c.insets = new Insets(8, 0, 4, 0);
        c.gridy = fila++;
        tarjeta.add(etiquetaCuotas, c);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridy = fila++;
        cuotas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        cuotas.setPreferredSize(new Dimension(0, EstilosUI.ALTO_CONTROL));
        tarjeta.add(cuotas, c);

        c.insets = new Insets(12, 0, 10, 0);
        c.gridy = fila++;
        tarjeta.add(notaPago, c);

        c.gridy = fila++;
        c.weighty = 1;
        c.anchor = GridBagConstraints.SOUTH;
        EstilosUI.estilizarBotonPrincipal(facturar);
        facturar.setPreferredSize(new Dimension(0, 42));
        tarjeta.add(facturar, c);
        return tarjeta;
    }

    private static JLabel valor() {
        JLabel label = new JLabel("$0.00", SwingConstants.RIGHT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(EstilosUI.TINTA);
        return label;
    }

    private void actualizarCamposPago() {
        boolean enEfectivo = efectivo.isSelected();
        nombre.setEnabled(enEfectivo);
        etiquetaNombre.setEnabled(enEfectivo);
        cuotas.setEnabled(!enEfectivo);
        etiquetaCuotas.setEnabled(!enEfectivo);
        notaPago.setText(enEfectivo ? "<html>Se aplica el 42% de descuento sobre el subtotal.</html>"
            : "<html>El banco verifica si es sujeto de crédito y su monto máximo; el nombre se toma del banco.</html>");
    }

    public void alAgregar(ActionListener l) { agregar.addActionListener(l); }
    public void alQuitar(ActionListener l) { quitar.addActionListener(l); }
    public void alVaciar(ActionListener l) { vaciar.addActionListener(l); }
    public void alCambiarFormaPago(ActionListener l) { efectivo.addActionListener(l); credito.addActionListener(l); }
    public void alFacturar(ActionListener l) { facturar.addActionListener(l); }

    public void setCelulares(List<Celular> lista) {
        celulares.removeAllItems();
        lista.forEach(celulares::addItem);
    }

    public Celular celularSeleccionado() { return (Celular) celulares.getSelectedItem(); }
    public int cantidad() { return (Integer) cantidad.getValue(); }
    public int lineaSeleccionada() { return carrito.getSelectedRow(); }
    public String formaPago() { return efectivo.isSelected() ? CarritoVenta.EFECTIVO : CarritoVenta.CREDITO; }
    public String cedula() { return cedula.getText().trim(); }
    public String nombre() { return nombre.getText().trim(); }
    public int cuotas() { return (Integer) cuotas.getValue(); }

    public void mostrarCarrito(List<CarritoVenta.Linea> lineas, BigDecimal sub, BigDecimal desc, BigDecimal tot) {
        modeloCarrito.setFilas(lineas);
        subtotal.setText(Formato.dinero(sub));
        descuento.setText(desc.signum() == 0 ? Formato.dinero(desc) : "- " + Formato.dinero(desc));
        total.setText(Formato.dinero(tot));
        cantidad.setValue(1);
    }

    /** Deja el formulario listo para la siguiente venta. */
    public void limpiar() {
        cedula.setText("");
        nombre.setText("");
        cuotas.setValue(6);
        efectivo.setSelected(true);
        actualizarCamposPago();
    }
}
