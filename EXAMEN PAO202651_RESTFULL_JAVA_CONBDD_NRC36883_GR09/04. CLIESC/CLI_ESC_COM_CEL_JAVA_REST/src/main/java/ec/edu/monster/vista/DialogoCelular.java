package ec.edu.monster.vista;

import ec.edu.monster.modelo.Celular;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Formulario modal para registrar o editar un celular; devuelve null si se cancela. */
public final class DialogoCelular extends JDialog {
    private final JTextField marca = new JTextField();
    private final JTextField modelo = new JTextField();
    private final JTextField descripcion = new JTextField();
    private final JTextField precio = new JTextField();
    private final JCheckBox disponible = new JCheckBox("Disponible para la venta");
    private Celular resultado;

    private DialogoCelular(JFrame padre, Celular actual) {
        super(padre, actual == null ? "Nuevo celular" : "Editar celular", true);
        JPanel raiz = new JPanel(new BorderLayout(0, 16));
        raiz.setBackground(EstilosUI.BLANCO);
        raiz.setBorder(BorderFactory.createEmptyBorder(22, 26, 20, 26));
        JLabel titulo = new JLabel(actual == null ? "Registrar celular" : "Editar " + actual.nombre());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(EstilosUI.TINTA);
        raiz.add(titulo, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 0, 5, 0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.gridx = 0;
        int fila = 0;
        for (Object[] campo : new Object[][] {
                {"Marca", marca}, {"Modelo", modelo}, {"Descripción (opcional)", descripcion}, {"Precio (USD)", precio}}) {
            c.gridy = fila++;
            formulario.add(EstilosUI.etiqueta((String) campo[0]), c);
            c.gridy = fila++;
            JTextField texto = (JTextField) campo[1];
            EstilosUI.estilizarCampo(texto);
            texto.setPreferredSize(new java.awt.Dimension(380, EstilosUI.ALTO_CONTROL));
            formulario.add(texto, c);
        }
        if (actual != null) {
            marca.setText(actual.getMarca());
            modelo.setText(actual.getModelo());
            descripcion.setText(actual.getDescripcion() == null ? "" : actual.getDescripcion());
            precio.setText(actual.getPrecio().toPlainString());
            disponible.setSelected(!Boolean.FALSE.equals(actual.getDisponible()));
            disponible.setOpaque(false);
            disponible.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            c.gridy = fila;
            formulario.add(disponible, c);
        }
        raiz.add(formulario, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botones.setOpaque(false);
        JButton cancelar = new JButton("Cancelar");
        JButton guardar = new JButton("Guardar");
        EstilosUI.estilizarBotonSecundario(cancelar);
        EstilosUI.estilizarBotonPrincipal(guardar);
        cancelar.addActionListener(e -> dispose());
        guardar.addActionListener(e -> guardar(actual));
        botones.add(cancelar);
        botones.add(guardar);
        raiz.add(botones, BorderLayout.SOUTH);

        setContentPane(raiz);
        getRootPane().setDefaultButton(guardar);
        pack();
        setResizable(false);
        setLocationRelativeTo(padre);
    }

    /** Muestra el formulario; actual es null para un celular nuevo. */
    public static Celular mostrar(JFrame padre, Celular actual) {
        DialogoCelular dialogo = new DialogoCelular(padre, actual);
        dialogo.setVisible(true);
        return dialogo.resultado;
    }

    private void guardar(Celular actual) {
        String error = null;
        BigDecimal valor = null;
        if (marca.getText().isBlank()) error = "Escribe la marca.";
        else if (modelo.getText().isBlank()) error = "Escribe el modelo.";
        else {
            try {
                valor = new BigDecimal(precio.getText().trim().replace(',', '.'));
                if (valor.signum() <= 0 || valor.stripTrailingZeros().scale() > 2)
                    error = "El precio debe ser mayor que cero y tener hasta dos decimales.";
            } catch (NumberFormatException ex) {
                error = "Escribe un precio válido; puedes usar punto o coma decimal.";
            }
        }
        if (error != null) {
            JOptionPane.showMessageDialog(this, error, "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Celular celular = new Celular(marca.getText().trim(), modelo.getText().trim(),
            descripcion.getText().isBlank() ? null : descripcion.getText().trim(), valor);
        if (actual != null) celular.setDisponible(disponible.isSelected());
        resultado = celular;
        dispose();
    }
}
