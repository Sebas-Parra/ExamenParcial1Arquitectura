package ec.edu.monster.consola.vista;

import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.RespuestaRest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

/** Presenta la interfaz de texto y recoge las entradas del usuario. */
public final class VistaConsola {
    private static final String TITULO = "COMERCIALIZADORA DE TELÉFONOS CELULARES";
    private static final String SEPARADOR = "------------------------------------------------------------------------";
    private final BufferedReader entrada;
    private final PrintWriter salida;
    private final boolean interactiva;

    public VistaConsola(BufferedReader entrada, PrintWriter salida) {
        this(entrada, salida, false);
    }

    public VistaConsola(BufferedReader entrada, PrintWriter salida, boolean interactiva) {
        this.entrada = entrada;
        this.salida = salida;
        this.interactiva = interactiva;
    }

    public void mostrarBienvenida() {
        salida.println(TITULO);
    }

    public void mostrarMenuPrincipal() {
        imprimirSeccion("MENÚ PRINCIPAL");
        salida.println("Catálogo:");
        salida.println("  1. Consultar catálogo de celulares");
        salida.println("  2. Registrar celular");
        salida.println("  3. Actualizar celular");
        salida.println("  4. Retirar celular del catálogo");
        salida.println();
        salida.println("Ventas:");
        salida.println("  5. Facturar venta (efectivo o crédito directo BanQuito)");
        salida.println("  6. Consultar facturas");
        salida.println("  7. Consultar tabla de amortización de un crédito");
        salida.println();
        salida.println("  0. Salir");
        salida.println(SEPARADOR);
    }

    public void mostrarSeccion(String titulo) {
        imprimirSeccion(titulo);
    }

    public void esperarParaContinuar() throws IOException {
        if (!interactiva) return;
        salida.println();
        salida.println(SEPARADOR);
        leerLiteral("Presiona Enter para volver al menú principal...");
    }

    public String leer(String mensaje) throws IOException {
        return leerLiteral(mensaje).trim();
    }

    public String leerLiteral(String mensaje) throws IOException {
        salida.print(mensaje);
        salida.flush();
        String linea = entrada.readLine();
        if (linea == null) throw new FinEntrada();
        return linea;
    }

    /** Lee un texto; con valor actual, Enter lo conserva. Si es obligatorio no acepta vacío. */
    public String leerTexto(String etiqueta, String actual, boolean obligatorio) throws IOException {
        String indicacion = actual == null ? etiqueta + ": " : etiqueta + " [" + actual + "]: ";
        while (true) {
            String valor = leer(indicacion);
            if (valor.isEmpty() && actual != null) return actual;
            if (!valor.isEmpty() || !obligatorio) return valor;
            salida.println("Este dato es obligatorio.");
        }
    }

    /** Lee un precio mayor que cero con hasta dos decimales; con valor actual, Enter lo conserva. */
    public BigDecimal leerPrecio(BigDecimal actual) throws IOException {
        String indicacion = actual == null ? "Precio: " : "Precio [" + dinero(actual) + "]: ";
        while (true) {
            String valor = leer(indicacion).replace(',', '.');
            if (valor.isEmpty() && actual != null) return actual;
            try {
                BigDecimal precio = new BigDecimal(valor);
                if (precio.signum() > 0 && precio.stripTrailingZeros().scale() <= 2) return precio;
            } catch (NumberFormatException e) {
                // Volver a solicitar el precio.
            }
            salida.println("Ingrese un valor mayor que cero con hasta dos decimales; puede usar punto o coma.");
        }
    }

    public int leerEntero(String mensaje, int minimo, int maximo) throws IOException {
        while (true) {
            try {
                int valor = Integer.parseInt(leer(mensaje));
                if (valor >= minimo && valor <= maximo) return valor;
            } catch (NumberFormatException e) {
                // Volver a solicitar el número.
            }
            salida.println("Ingrese un número entero entre " + minimo + " y " + maximo + ".");
        }
    }

    /** Lee una cédula de 10 dígitos; si se permite vacío, Enter devuelve una cadena vacía. */
    public String leerCedula(String mensaje, boolean permitirVacio) throws IOException {
        while (true) {
            String cedula = leer(mensaje);
            if (cedula.matches("\\d{10}") || (permitirVacio && cedula.isEmpty())) return cedula;
            salida.println("La cédula debe tener 10 dígitos.");
        }
    }

    public boolean confirmar(String mensaje) throws IOException {
        while (true) {
            String respuesta = leer(mensaje + " (s/n): ").toLowerCase(Locale.ROOT);
            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) return true;
            if (respuesta.equals("n") || respuesta.equals("no")) return false;
            salida.println("Responda s o n.");
        }
    }

    /** Muestra una lista numerada y devuelve la opción elegida, o null si se elige 0. */
    public <T> T seleccionar(String titulo, List<T> opciones, Function<T, String> etiqueta, String volver)
            throws IOException {
        if (opciones == null || opciones.isEmpty()) {
            salida.println("No hay opciones disponibles.");
            return null;
        }
        salida.println();
        salida.println(titulo + ":");
        salida.println();
        for (int i = 0; i < opciones.size(); i++) {
            salida.printf("  %2d. %s%n", i + 1, etiqueta.apply(opciones.get(i)));
        }
        salida.println("   0. " + volver);
        salida.println();
        int opcion = leerEntero("Opción (0-" + opciones.size() + "): ", 0, opciones.size());
        return opcion == 0 ? null : opciones.get(opcion - 1);
    }

    public void mostrarRespuesta(RespuestaRest respuesta) {
        String codigo = respuesta.getCodigo();
        if (codigo == null || codigo.isBlank()) codigo = "HTTP " + respuesta.getEstadoHttp();
        String mensaje = respuesta.getMensaje();
        if (mensaje == null || mensaje.isBlank()) mensaje = "El servidor no envió un mensaje.";
        salida.println();
        salida.println((respuesta.isExito() ? "" : "[ERROR] ") + codigo + ": " + mensaje);
        if (respuesta.isExito()) return;
        if (respuesta.getMotivo() != null) salida.println("Motivo: " + respuesta.getMotivo());
        if (respuesta.getMontoMaximo() != null)
            salida.println("Monto máximo de crédito aprobado: " + dinero(respuesta.getMontoMaximo()));
    }

    public void mostrarError(String mensaje) {
        salida.println();
        salida.println("Error: " + mensaje);
    }

    public void mostrarMensaje(String mensaje) {
        salida.println(mensaje);
    }

    public void mostrarCatalogo(List<Celular> celulares) {
        salida.println();
        if (celulares.isEmpty()) {
            salida.println("No hay celulares en el catálogo.");
            return;
        }
        salida.printf("%-5s %-30s %12s  %s%n", "Cód.", "Celular", "Precio", "Descripción");
        salida.println(SEPARADOR);
        for (Celular c : celulares) {
            String descripcion = c.getDescripcion() == null ? "" : c.getDescripcion();
            if (Boolean.FALSE.equals(c.getDisponible())) descripcion = "(retirado) " + descripcion;
            salida.printf("%-5d %-30s %12s  %s%n", c.getCodCelular(), recortar(c.nombre(), 30),
                dinero(c.getPrecio()), descripcion);
        }
    }

    public void mostrarCelular(Celular c) {
        salida.println();
        salida.println("Código:      " + c.getCodCelular());
        salida.println("Celular:     " + c.nombre());
        salida.println("Descripción: " + (c.getDescripcion() == null ? "-" : c.getDescripcion()));
        salida.println("Precio:      " + dinero(c.getPrecio()));
        salida.println("Disponible:  " + (Boolean.FALSE.equals(c.getDisponible()) ? "no (retirado)" : "sí"));
    }

    public void mostrarCarrito(Map<Celular, Integer> carrito) {
        if (carrito.isEmpty()) {
            salida.println("Carrito vacío.");
            return;
        }
        salida.println("Carrito:");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Map.Entry<Celular, Integer> linea : carrito.entrySet()) {
            BigDecimal valor = linea.getKey().getPrecio().multiply(BigDecimal.valueOf(linea.getValue()));
            salida.printf("  %3d x %-30s %12s%n", linea.getValue(), recortar(linea.getKey().nombre(), 30), dinero(valor));
            subtotal = subtotal.add(valor);
        }
        salida.printf("  %-36s %12s%n", "Subtotal", dinero(subtotal));
    }

    public void mostrarFactura(Factura f) {
        salida.println();
        salida.println(SEPARADOR);
        salida.println("FACTURA N.º " + f.getCodFactura() + "    Fecha: " + texto(f.getFecha()));
        salida.println("Cliente: " + texto(f.getNombre()) + "    Cédula: " + f.getCedula());
        salida.println("Forma de pago: " + ("CREDITO".equals(f.getFormaPago()) ? "Crédito directo BanQuito" : "Efectivo"));
        salida.println(SEPARADOR);
        salida.printf("%-5s %-30s %6s %12s %12s%n", "Cód.", "Celular", "Cant.", "P. unitario", "Subtotal");
        for (DetalleFactura d : f.getDetalles()) {
            salida.printf("%-5d %-30s %6d %12s %12s%n", d.getCodCelular(), recortar(d.getMarca() + " " + d.getModelo(), 30),
                d.getCantidad(), dinero(d.getPrecioUnitario()), dinero(d.getSubtotal()));
        }
        salida.println(SEPARADOR);
        salida.printf("%57s %12s%n", "Subtotal:", dinero(f.getSubtotal()));
        salida.printf("%57s %12s%n", "EFECTIVO".equals(f.getFormaPago()) ? "Descuento efectivo (42%):" : "Descuento:",
            dinero(f.getDescuento()));
        salida.printf("%57s %12s%n", "TOTAL:", dinero(f.getTotal()));
        if (f.getCodCredito() != null) {
            salida.println();
            salida.println("Crédito BanQuito N.º " + f.getCodCredito() + ": " + f.getNumeroCuotas()
                + " cuotas mensuales de " + dinero(f.getValorCuota()));
        }
        salida.println(SEPARADOR);
    }

    public String resumenFactura(Factura f) {
        return String.format(Locale.ROOT, "N.º %-4d %-19s %-10s %-26s %10s",
            f.getCodFactura(), texto(f.getFecha()).replace('T', ' '), f.getFormaPago(),
            recortar(texto(f.getNombre()), 26), dinero(f.getTotal()));
    }

    public void mostrarTablaAmortizacion(RespuestaRest r) {
        salida.println();
        if (r.getNombre() != null) salida.println("Cliente: " + r.getNombre());
        if (r.getCodCredito() != null && r.getMonto() != null)
            salida.println("Crédito N.º " + r.getCodCredito() + "    Monto: " + dinero(r.getMonto())
                + "    Tasa anual: " + (r.getTasaAnual() == null ? "-" : r.getTasaAnual().toPlainString() + "%")
                + "    Plazo: " + (r.getNumeroCuotas() == null ? r.getTablaAmortizacion().size() : r.getNumeroCuotas())
                + " meses");
        salida.println(SEPARADOR);
        salida.printf("%8s %14s %16s %16s %14s%n", "# Cuota", "Valor Cuota", "Interés Pagado", "Capital Pagado", "Saldo");
        salida.println(SEPARADOR);
        for (CuotaAmortizacion c : CuotaAmortizacion.conFilaInicial(r.getTablaAmortizacion(), r.getMonto())) {
            salida.printf("%8d %14s %16s %16s %14s%n", c.getNumeroCuota(), celda(c.getValorCuota()),
                celda(c.getInteresPagado()), celda(c.getCapitalPagado()), celda(c.getSaldo()));
        }
        salida.println(SEPARADOR);
    }

    public static String dinero(BigDecimal valor) {
        return valor == null ? "-" : String.format(Locale.ROOT, "$%,.2f", valor);
    }

    /** Celda de la tabla de amortización: vacía cuando no aplica (fila 0). */
    private static String celda(BigDecimal valor) {
        return valor == null ? "" : dinero(valor);
    }

    private static String texto(String valor) {
        return valor == null ? "-" : valor;
    }

    private static String recortar(String valor, int largo) {
        return valor.length() <= largo ? valor : valor.substring(0, largo - 1) + "…";
    }

    private void imprimirSeccion(String titulo) {
        limpiarPantalla();
        salida.println();
        salida.println(TITULO);
        salida.println(SEPARADOR);
        salida.println(titulo);
        salida.println(SEPARADOR);
        salida.println();
    }

    private void limpiarPantalla() {
        if (!interactiva) return;
        if (System.console() != null) {
            if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
                salida.flush();
                try {
                    if (new ProcessBuilder("cmd.exe", "/c", "cls").inheritIO().start().waitFor() == 0) return;
                } catch (IOException e) {
                    // Separar con espacios cuando la terminal no permite limpiar.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                salida.print("\033[H\033[2J");
                salida.flush();
                return;
            }
        }
        // La salida del IDE conserva su historial: desplazarlo deja visible la etapa actual.
        salida.print("\n".repeat(24));
        salida.flush();
    }

    public static final class FinEntrada extends RuntimeException {
    }
}
