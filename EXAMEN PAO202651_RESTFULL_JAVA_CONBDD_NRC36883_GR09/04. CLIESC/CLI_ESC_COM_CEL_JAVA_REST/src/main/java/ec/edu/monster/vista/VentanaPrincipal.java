package ec.edu.monster.vista;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

/** Ventana principal: navegación lateral entre catálogo, facturación y consultas. */
public final class VentanaPrincipal extends JFrame {
    public static final String CATALOGO = "catalogo";
    public static final String FACTURAR = "facturar";
    public static final String FACTURAS = "facturas";
    public static final String AMORTIZACION = "amortizacion";

    private record Seccion(String titulo, String detalle) { }

    private static final Map<String, Seccion> SECCIONES = new LinkedHashMap<>();
    static {
        SECCIONES.put(CATALOGO, new Seccion("Catálogo de celulares",
            "Registra, actualiza y retira los teléfonos disponibles para la venta."));
        SECCIONES.put(FACTURAR, new Seccion("Facturar venta",
            "Efectivo con 42% de descuento o crédito directo del Banco BanQuito."));
        SECCIONES.put(FACTURAS, new Seccion("Facturas",
            "Consulta las ventas registradas y su detalle."));
        SECCIONES.put(AMORTIZACION, new Seccion("Tabla de amortización",
            "Consulta en el Banco BanQuito los créditos de un cliente y sus cuotas."));
    }

    private final PanelCatalogo catalogo = new PanelCatalogo();
    private final PanelFacturacion facturacion = new PanelFacturacion();
    private final PanelFacturas facturas = new PanelFacturas();
    private final PanelAmortizacion amortizacion = new PanelAmortizacion();
    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenido = new JPanel(tarjetas);
    private final Map<String, JButton> botones = new LinkedHashMap<>();
    private final JLabel titulo = new JLabel();
    private final JLabel detalle = new JLabel();
    private final JLabel estado = new JLabel(" ");
    private final JPanel bloqueo = new JPanel();
    private Consumer<String> alCambiarSeccion = clave -> { };

    public VentanaPrincipal(String servidor) {
        super("Monster · Comercializadora de Teléfonos Celulares");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EstilosUI.configurarVentana(this);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(EstilosUI.FONDO);
        raiz.add(crearNavegacion(), BorderLayout.WEST);

        JPanel centro = new JPanel(new BorderLayout(0, 18));
        centro.setOpaque(false);
        centro.setBorder(BorderFactory.createEmptyBorder(24, 30, 14, 30));
        centro.add(crearCabecera(), BorderLayout.NORTH);
        contenido.setOpaque(false);
        contenido.add(catalogo, CATALOGO);
        contenido.add(facturacion, FACTURAR);
        contenido.add(facturas, FACTURAS);
        contenido.add(amortizacion, AMORTIZACION);
        centro.add(contenido, BorderLayout.CENTER);
        centro.add(crearPie(servidor), BorderLayout.SOUTH);
        raiz.add(centro, BorderLayout.CENTER);
        setContentPane(raiz);

        // Mientras hay una llamada en curso, el panel de vidrio bloquea los clics y muestra el cursor de espera.
        bloqueo.setOpaque(false);
        bloqueo.addMouseListener(new MouseAdapter() { });
        bloqueo.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setGlassPane(bloqueo);
    }

    private JPanel crearNavegacion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EstilosUI.AZUL_MARINO);
        panel.setPreferredSize(new Dimension(240, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(28, 0, 22, 0));

        JPanel marca = new JPanel(new GridLayout(0, 1, 0, 4));
        marca.setOpaque(false);
        marca.setBorder(BorderFactory.createEmptyBorder(0, 24, 26, 20));
        JLabel nombre = new JLabel("MONSTER");
        nombre.setFont(new Font("Segoe UI", Font.BOLD, 22));
        nombre.setForeground(EstilosUI.BLANCO);
        JLabel subtitulo = new JLabel("Comercializadora de celulares");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(new Color(190, 204, 224));
        marca.add(nombre);
        marca.add(subtitulo);
        panel.add(marca, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(0, 1, 0, 4));
        menu.setOpaque(false);
        String[] etiquetas = {"Catálogo", "Facturar venta", "Facturas", "Tabla de amortización"};
        int i = 0;
        for (String clave : SECCIONES.keySet()) {
            JButton boton = botonNavegacion(etiquetas[i++]);
            boton.addActionListener(e -> alCambiarSeccion.accept(clave));
            botones.put(clave, boton);
            menu.add(boton);
        }
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setOpaque(false);
        contenedor.add(menu, BorderLayout.NORTH);
        panel.add(contenedor, BorderLayout.CENTER);

        JLabel aliado = new JLabel("<html>Crédito directo<br>Banco BanQuito · 16.5% anual</html>");
        aliado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        aliado.setForeground(new Color(190, 204, 224));
        aliado.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 20));
        panel.add(aliado, BorderLayout.SOUTH);
        return panel;
    }

    private static JButton botonNavegacion(String texto) {
        JButton boton = new JButton(texto);
        boton.setUI(new BasicButtonUI());
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setForeground(new Color(220, 228, 240));
        boton.setBackground(EstilosUI.AZUL_MARINO);
        boton.setOpaque(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setBorder(BorderFactory.createEmptyBorder(12, 24, 12, 20));
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return boton;
    }

    private JPanel crearCabecera() {
        JPanel textos = new JPanel(new GridLayout(0, 1, 0, 5));
        textos.setOpaque(false);
        JLabel marca = new JLabel("MONSTER  /  SERVICIO REST");
        marca.setFont(new Font("Segoe UI", Font.BOLD, 12));
        marca.setForeground(EstilosUI.AZUL_MARINO);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        titulo.setForeground(EstilosUI.TINTA);
        detalle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        detalle.setForeground(EstilosUI.GRIS);
        textos.add(marca);
        textos.add(titulo);
        textos.add(detalle);
        return textos;
    }

    private JPanel crearPie(String servidor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        estado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        estado.setForeground(EstilosUI.GRIS);
        JLabel url = new JLabel(servidor);
        url.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        url.setForeground(EstilosUI.GRIS);
        panel.add(estado, BorderLayout.WEST);
        panel.add(url, BorderLayout.EAST);
        return panel;
    }

    public PanelCatalogo catalogo() { return catalogo; }
    public PanelFacturacion facturacion() { return facturacion; }
    public PanelFacturas facturas() { return facturas; }
    public PanelAmortizacion amortizacion() { return amortizacion; }

    public void alCambiarSeccion(Consumer<String> listener) { alCambiarSeccion = listener; }

    public void mostrarSeccion(String clave) {
        Seccion seccion = SECCIONES.get(clave);
        titulo.setText(seccion.titulo());
        detalle.setText(seccion.detalle());
        tarjetas.show(contenido, clave);
        botones.forEach((k, boton) -> {
            boolean activo = k.equals(clave);
            boton.setBackground(activo ? EstilosUI.AZUL_HOVER : EstilosUI.AZUL_MARINO);
            boton.setForeground(activo ? EstilosUI.BLANCO : new Color(220, 228, 240));
            boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, activo ? new Color(120, 170, 235) : EstilosUI.AZUL_MARINO),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));
        });
    }

    public void mostrarEstado(String texto) { estado.setText(texto == null || texto.isBlank() ? " " : texto); }

    public void setOcupado(boolean ocupado) {
        bloqueo.setVisible(ocupado);
    }

    public void mostrarAviso(String texto, String tituloAviso, int tipo) {
        JOptionPane.showMessageDialog(this, texto, tituloAviso, tipo);
    }

    public boolean confirmar(String texto, String tituloAviso) {
        return JOptionPane.showConfirmDialog(this, texto, tituloAviso, JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }
}
