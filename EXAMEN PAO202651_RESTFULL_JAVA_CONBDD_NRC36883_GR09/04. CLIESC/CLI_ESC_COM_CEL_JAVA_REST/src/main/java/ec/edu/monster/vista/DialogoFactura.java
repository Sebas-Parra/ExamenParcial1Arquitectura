package ec.edu.monster.vista;

import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.Factura;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/** Muestra una factura con su detalle y, si es a crédito, la tabla de amortización. */
public final class DialogoFactura extends JDialog {
    private DialogoFactura(JFrame padre, String encabezado, Factura f, List<CuotaAmortizacion> tabla) {
        super(padre, "Factura N.º " + f.getCodFactura(), true);
        JPanel raiz = new JPanel(new BorderLayout(0, 14));
        raiz.setBackground(EstilosUI.FONDO);
        raiz.setBorder(BorderFactory.createEmptyBorder(20, 24, 18, 24));

        JPanel cabecera = new JPanel(new GridLayout(0, 1, 0, 4));
        cabecera.setOpaque(false);
        if (encabezado != null) {
            JLabel aviso = new JLabel(encabezado);
            aviso.setFont(new Font("Segoe UI", Font.BOLD, 13));
            aviso.setForeground(new java.awt.Color(30, 120, 70));
            cabecera.add(aviso);
        }
        JLabel titulo = new JLabel("Factura N.º " + f.getCodFactura() + "  ·  " + Formato.fecha(f.getFecha()));
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(EstilosUI.TINTA);
        cabecera.add(titulo);
        cabecera.add(EstilosUI.ayuda("Cliente: " + f.getNombre() + "   ·   Cédula: " + f.getCedula()
            + "   ·   Forma de pago: " + Formato.formaPago(f.getFormaPago())));
        raiz.add(cabecera, BorderLayout.NORTH);

        JPanel cuerpo = new JPanel(new BorderLayout(0, 14));
        cuerpo.setOpaque(false);
        ModeloTabla<DetalleFactura> detalle = new ModeloTabla<>(
            ModeloTabla.Columna.numero("Cód.", DetalleFactura::getCodCelular),
            ModeloTabla.Columna.texto("Celular", d -> d.getMarca() + " " + d.getModelo()),
            ModeloTabla.Columna.numero("Cantidad", DetalleFactura::getCantidad),
            ModeloTabla.Columna.numero("P. unitario", d -> Formato.dinero(d.getPrecioUnitario())),
            ModeloTabla.Columna.numero("Subtotal", d -> Formato.dinero(d.getSubtotal())));
        detalle.setFilas(f.getDetalles());
        JTable tablaDetalle = EstilosUI.tabla(detalle);
        tablaDetalle.getColumnModel().getColumn(0).setMaxWidth(60);
        JPanel tarjetaDetalle = EstilosUI.tarjeta(new BorderLayout(0, 10));
        tarjetaDetalle.add(EstilosUI.desplazable(tablaDetalle), BorderLayout.CENTER);
        tarjetaDetalle.add(totales(f), BorderLayout.SOUTH);
        tarjetaDetalle.setPreferredSize(new Dimension(760, tabla.isEmpty() ? 300 : 250));
        cuerpo.add(tarjetaDetalle, BorderLayout.NORTH);

        if (!tabla.isEmpty()) {
            JPanel tarjetaCredito = EstilosUI.tarjeta(new BorderLayout(0, 10));
            JPanel textos = new JPanel(new GridLayout(0, 1, 0, 4));
            textos.setOpaque(false);
            textos.add(EstilosUI.tituloTarjeta("Tabla de amortización · Crédito BanQuito N.º " + f.getCodCredito()));
            textos.add(EstilosUI.ayuda(f.getNumeroCuotas() + " cuotas mensuales de " + Formato.dinero(f.getValorCuota())
                + " · tasa 16.5% anual"));
            tarjetaCredito.add(textos, BorderLayout.NORTH);
            ModeloTabla<CuotaAmortizacion> cuotas = TablaAmortizacion.modelo();
            cuotas.setFilas(CuotaAmortizacion.conFilaInicial(tabla, f.getTotal()));
            tarjetaCredito.add(EstilosUI.desplazable(EstilosUI.tabla(cuotas)), BorderLayout.CENTER);
            tarjetaCredito.setPreferredSize(new Dimension(760, 300));
            cuerpo.add(tarjetaCredito, BorderLayout.CENTER);
        }
        raiz.add(cuerpo, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.setOpaque(false);
        JButton guardarPdf = new JButton("Descargar PDF");
        EstilosUI.estilizarBotonSecundario(guardarPdf);
        guardarPdf.addActionListener(e -> guardarPdf(f));
        botones.add(guardarPdf);
        JButton cerrar = new JButton("Cerrar");
        EstilosUI.estilizarBotonPrincipal(cerrar);
        cerrar.addActionListener(e -> dispose());
        botones.add(cerrar);
        raiz.add(botones, BorderLayout.SOUTH);

        setContentPane(raiz);
        getRootPane().setDefaultButton(cerrar);
        pack();
        setLocationRelativeTo(padre);
    }

    private void guardarPdf(Factura factura) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar factura como PDF");
        selector.setFileFilter(new FileNameExtensionFilter("Archivo PDF (*.pdf)", "pdf"));
        selector.setSelectedFile(new File("factura-" + factura.getCodFactura() + ".pdf"));
        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")) {
            archivo = new File(archivo.getParentFile(), archivo.getName() + ".pdf");
        }
        if (archivo.exists() && JOptionPane.showConfirmDialog(this,
                "El archivo ya existe. ¿Deseas reemplazarlo?", "Confirmar reemplazo",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION) return;

        try {
            Files.write(archivo.toPath(), GeneradorPdfFactura.generar(factura));
            JOptionPane.showMessageDialog(this, "Factura guardada en:\n" + archivo.getAbsolutePath(),
                    "PDF guardado", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar el PDF: " + ex.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JPanel totales(Factura f) {
        JPanel valores = new JPanel(new GridLayout(0, 2, 24, 4));
        valores.setOpaque(false);
        valores.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 6));
        agregar(valores, "Subtotal", Formato.dinero(f.getSubtotal()), false);
        agregar(valores, "EFECTIVO".equals(f.getFormaPago()) ? "Descuento efectivo (42%)" : "Descuento",
            (f.getDescuento() == null || f.getDescuento().signum() == 0 ? "" : "- ") + Formato.dinero(f.getDescuento()), false);
        agregar(valores, "TOTAL", Formato.dinero(f.getTotal()), true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(valores, BorderLayout.EAST);
        return panel;
    }

    private static void agregar(JPanel panel, String etiqueta, String valor, boolean destacado) {
        JLabel izquierda = destacado ? EstilosUI.etiqueta(etiqueta) : EstilosUI.ayuda(etiqueta);
        JLabel derecha = new JLabel(valor, SwingConstants.RIGHT);
        derecha.setFont(new Font("Segoe UI", destacado ? Font.BOLD : Font.PLAIN, destacado ? 16 : 14));
        derecha.setForeground(destacado ? EstilosUI.AZUL_MARINO : EstilosUI.TINTA);
        panel.add(izquierda);
        panel.add(derecha);
    }

    /** Muestra la factura; encabezado es un aviso opcional (por ejemplo, tras facturar). */
    public static void mostrar(JFrame padre, String encabezado, Factura factura, List<CuotaAmortizacion> tabla) {
        new DialogoFactura(padre, encabezado, factura, tabla == null ? List.of() : tabla).setVisible(true);
    }
}
