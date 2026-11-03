package ec.edu.monster.vista;

import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

/** Consulta, a través de la comercializadora, de los créditos de un cliente y su tabla de amortización. */
public final class PanelAmortizacion extends JPanel {
    private final JTextField cedula = new JTextField();
    private final JButton buscar = new JButton("Buscar créditos");
    private final ModeloTabla<Credito> modeloCreditos = new ModeloTabla<>(
        ModeloTabla.Columna.numero("N.º", Credito::getCodCredito),
        ModeloTabla.Columna.texto("Fecha", Credito::getFecha),
        ModeloTabla.Columna.numero("Monto", c -> Formato.dinero(c.getMonto())),
        ModeloTabla.Columna.numero("Cuotas", Credito::getNumeroCuotas),
        ModeloTabla.Columna.texto("Estado", Credito::getEstado));
    private final JTable creditos = EstilosUI.tabla(modeloCreditos);
    private final ModeloTabla<CuotaAmortizacion> modeloCuotas = TablaAmortizacion.modelo();
    private final JTable cuotas = EstilosUI.tabla(modeloCuotas);
    private final JLabel cliente = EstilosUI.tituloTarjeta("Créditos del cliente");
    private final JLabel resumen = EstilosUI.ayuda("Seleccione un crédito para ver su tabla de amortización.");

    PanelAmortizacion() {
        super(new BorderLayout(0, 14));
        setOpaque(false);
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        barra.setOpaque(false);
        barra.add(EstilosUI.etiqueta("Cédula del cliente"));
        EstilosUI.estilizarCampo(cedula);
        cedula.setPreferredSize(new Dimension(200, EstilosUI.ALTO_CONTROL));
        barra.add(cedula);
        EstilosUI.estilizarBotonPrincipal(buscar);
        barra.add(buscar);
        add(barra, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(16, 0));
        cuerpo.setOpaque(false);
        JPanel izquierda = EstilosUI.tarjeta(new BorderLayout(0, 10));
        izquierda.setPreferredSize(new Dimension(390, 0));
        izquierda.add(cliente, BorderLayout.NORTH);
        int[] anchos = {35, 100, 85, 55, 75};
        for (int i = 0; i < anchos.length; i++) creditos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        izquierda.add(EstilosUI.desplazable(creditos), BorderLayout.CENTER);
        JPanel derecha = EstilosUI.tarjeta(new BorderLayout(0, 10));
        JPanel cabecera = new JPanel(new GridLayout(0, 1, 0, 4));
        cabecera.setOpaque(false);
        cabecera.add(EstilosUI.tituloTarjeta("Tabla de amortización"));
        cabecera.add(resumen);
        derecha.add(cabecera, BorderLayout.NORTH);
        int[] anchosCuotas = {60, 95, 125, 125, 95};
        for (int i = 0; i < anchosCuotas.length; i++) cuotas.getColumnModel().getColumn(i).setPreferredWidth(anchosCuotas[i]);
        derecha.add(EstilosUI.desplazable(cuotas), BorderLayout.CENTER);
        cuerpo.add(izquierda, BorderLayout.WEST);
        cuerpo.add(derecha, BorderLayout.CENTER);
        add(cuerpo, BorderLayout.CENTER);
    }

    public void alBuscar(ActionListener l) { buscar.addActionListener(l); cedula.addActionListener(l); }

    public void alSeleccionarCredito(Consumer<Credito> l) {
        creditos.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int fila = creditos.getSelectedRow();
            if (fila >= 0) l.accept(modeloCreditos.fila(creditos.convertRowIndexToModel(fila)));
        });
    }

    public String cedula() { return cedula.getText().trim(); }

    public void setCreditos(String nombreCliente, List<Credito> lista) {
        cliente.setText(nombreCliente == null ? "Créditos del cliente" : "Créditos de " + nombreCliente);
        modeloCreditos.setFilas(lista);
        limpiarTabla();
    }

    public void setTabla(String texto, List<CuotaAmortizacion> lista, BigDecimal monto) {
        resumen.setText(texto);
        modeloCuotas.setFilas(CuotaAmortizacion.conFilaInicial(lista, monto));
    }

    public void limpiarTabla() {
        setTabla("Seleccione un crédito para ver su tabla de amortización.", List.of(), null);
    }
}
