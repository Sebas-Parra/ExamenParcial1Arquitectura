package ec.edu.monster.vista;

import ec.edu.monster.modelo.Celular;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTable;

/** Mantenimiento del catálogo: lista de celulares y acciones de registro, edición y retiro. */
public final class PanelCatalogo extends JPanel {
    private final ModeloTabla<Celular> modelo = new ModeloTabla<>(
        ModeloTabla.Columna.numero("Cód.", Celular::getCodCelular),
        ModeloTabla.Columna.texto("Marca", Celular::getMarca),
        ModeloTabla.Columna.texto("Modelo", Celular::getModelo),
        ModeloTabla.Columna.texto("Descripción", Celular::getDescripcion),
        ModeloTabla.Columna.numero("Precio", c -> Formato.dinero(c.getPrecio())),
        ModeloTabla.Columna.texto("Estado", c -> Boolean.FALSE.equals(c.getDisponible()) ? "Retirado" : "Disponible"));
    private final JTable tabla = EstilosUI.tabla(modelo);
    private final JCheckBox retirados = new JCheckBox("Mostrar retirados");
    private final JButton nuevo = new JButton("Nuevo celular");
    private final JButton editar = new JButton("Editar");
    private final JButton retirar = new JButton("Retirar");
    private final JButton recargar = new JButton("Actualizar lista");
    private ActionListener alEditar = e -> { };

    PanelCatalogo() {
        super(new BorderLayout(0, 14));
        setOpaque(false);

        JPanel barra = new JPanel(new BorderLayout());
        barra.setOpaque(false);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        acciones.setOpaque(false);
        EstilosUI.estilizarBotonPrincipal(nuevo);
        EstilosUI.estilizarBotonSecundario(editar);
        EstilosUI.estilizarBotonSecundario(retirar);
        acciones.add(nuevo);
        acciones.add(editar);
        acciones.add(retirar);
        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        derecha.setOpaque(false);
        retirados.setOpaque(false);
        retirados.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        retirados.setForeground(EstilosUI.TINTA);
        EstilosUI.estilizarBotonSecundario(recargar);
        derecha.add(retirados);
        derecha.add(recargar);
        barra.add(acciones, BorderLayout.WEST);
        barra.add(derecha, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        tabla.getColumnModel().getColumn(0).setMaxWidth(70);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(260);
        tabla.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tabla.getSelectedRow() >= 0) alEditar.actionPerformed(null);
            }
        });
        add(EstilosUI.desplazable(tabla), BorderLayout.CENTER);
        add(EstilosUI.ayuda("Doble clic sobre un celular para editarlo. Retirar lo quita de la venta sin borrar sus facturas."),
            BorderLayout.SOUTH);
    }

    public void alNuevo(ActionListener l) { nuevo.addActionListener(l); }
    public void alEditar(ActionListener l) { alEditar = l; editar.addActionListener(l); }
    public void alRetirar(ActionListener l) { retirar.addActionListener(l); }
    public void alRecargar(ActionListener l) { recargar.addActionListener(l); retirados.addActionListener(l); }

    public boolean incluirRetirados() { return retirados.isSelected(); }

    public void setCelulares(List<Celular> celulares) { modelo.setFilas(celulares); }

    public Celular seleccionado() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.fila(tabla.convertRowIndexToModel(fila));
    }
}
