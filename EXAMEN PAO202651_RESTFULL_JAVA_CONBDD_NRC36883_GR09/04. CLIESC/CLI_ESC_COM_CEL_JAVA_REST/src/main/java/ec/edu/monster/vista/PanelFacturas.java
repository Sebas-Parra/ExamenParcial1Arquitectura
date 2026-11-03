package ec.edu.monster.vista;

import ec.edu.monster.modelo.Factura;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/** Consulta de facturas, todas o por cédula del comprador. */
public final class PanelFacturas extends JPanel {
    private final JTextField cedula = new JTextField();
    private final JButton buscar = new JButton("Buscar");
    private final JButton detalle = new JButton("Ver detalle");
    private final ModeloTabla<Factura> modelo = new ModeloTabla<>(
        ModeloTabla.Columna.numero("N.º", Factura::getCodFactura),
        ModeloTabla.Columna.texto("Fecha", f -> Formato.fecha(f.getFecha())),
        ModeloTabla.Columna.texto("Cédula", Factura::getCedula),
        ModeloTabla.Columna.texto("Cliente", Factura::getNombre),
        ModeloTabla.Columna.texto("Forma de pago", f -> Formato.formaPago(f.getFormaPago())),
        ModeloTabla.Columna.numero("Total", f -> Formato.dinero(f.getTotal())),
        ModeloTabla.Columna.numero("Crédito", f -> f.getCodCredito() == null ? "-"
            : "N.º " + f.getCodCredito() + " · " + f.getNumeroCuotas() + " cuotas"));
    private final JTable tabla = EstilosUI.tabla(modelo);
    private ActionListener alVerDetalle = e -> { };

    PanelFacturas() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setOpaque(false);
        barra.add(EstilosUI.etiqueta("Cédula del comprador"));
        EstilosUI.estilizarCampo(cedula);
        cedula.setPreferredSize(new Dimension(200, EstilosUI.ALTO_CONTROL));
        barra.add(cedula);
        EstilosUI.estilizarBotonPrincipal(buscar);
        EstilosUI.estilizarBotonSecundario(detalle);
        barra.add(buscar);
        barra.add(detalle);
        add(barra, BorderLayout.NORTH);

        int[] anchos = {50, 150, 110, 190, 120, 100, 160};
        for (int i = 0; i < anchos.length; i++) tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        tabla.getColumnModel().getColumn(0).setMaxWidth(60);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) alVerDetalle.actionPerformed(null);
            }
        });
        add(EstilosUI.desplazable(tabla), BorderLayout.CENTER);
        add(EstilosUI.ayuda("Deje la cédula vacía para ver todas las facturas. Doble clic para ver el detalle."),
            BorderLayout.SOUTH);
    }

    public void alBuscar(ActionListener l) { buscar.addActionListener(l); cedula.addActionListener(l); }
    public void alVerDetalle(ActionListener l) { alVerDetalle = l; detalle.addActionListener(l); }

    public String cedula() { return cedula.getText().trim(); }
    public void setFacturas(List<Factura> facturas) { modelo.setFilas(facturas); }

    public Factura seleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.fila(tabla.convertRowIndexToModel(fila));
    }
}
