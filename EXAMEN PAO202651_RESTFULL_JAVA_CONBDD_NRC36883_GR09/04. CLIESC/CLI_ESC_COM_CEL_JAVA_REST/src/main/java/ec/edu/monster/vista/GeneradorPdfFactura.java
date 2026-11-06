package ec.edu.monster.vista;

import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.CuotaAmortizacion;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Genera una factura descargable con una presentación compacta para papel A4. */
public final class GeneradorPdfFactura {
    private static final float ANCHO = PDRectangle.A4.getWidth();
    private static final float ALTO = PDRectangle.A4.getHeight();
    private static final float MARGEN = 42;
    private static final float ANCHO_CONTENIDO = ANCHO - MARGEN * 2;
    private static final Color AZUL = new Color(24, 50, 87);
    private static final Color AZUL_CLARO = new Color(232, 239, 248);
    private static final Color GRIS = new Color(91, 105, 122);
    private static final Color BORDE = new Color(211, 219, 229);
    private static final Color FILA = new Color(247, 249, 252);
    private static final PDType1Font NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    private GeneradorPdfFactura() { }

    public static byte[] generar(Factura factura) throws IOException {
        return generar(factura, List.of());
    }

    public static byte[] generar(Factura factura, List<CuotaAmortizacion> tabla) throws IOException {
        if (factura == null || factura.getCodFactura() == null) {
            throw new IllegalArgumentException("La factura no contiene un número válido.");
        }
        try (PDDocument documento = new PDDocument(); ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            documento.getDocumentInformation().setTitle("Factura N.° " + factura.getCodFactura());
            Pagina pagina = nuevaPagina(documento, factura, null);
            pagina.y = dibujarEncabezadoTabla(pagina.stream, pagina.y);

            int indice = 0;
            for (DetalleFactura detalle : factura.getDetalles()) {
                List<String> producto = envolver(texto(detalle.getMarca()) + " " + texto(detalle.getModelo()), 235, 9);
                float altoFila = Math.max(30, producto.size() * 12 + 14);
                if (pagina.y - altoFila < 120) {
                    cerrarPagina(pagina);
                    pagina = nuevaPagina(documento, factura, "Detalle de factura · continuación");
                    pagina.y = dibujarEncabezadoTabla(pagina.stream, pagina.y);
                    indice = 0;
                }
                dibujarFila(pagina.stream, pagina.y, altoFila, detalle, producto, indice++);
                pagina.y -= altoFila;
            }

            if (pagina.y < 245) {
                cerrarPagina(pagina);
                pagina = nuevaPagina(documento, factura, "Resumen de factura · N.° " + factura.getCodFactura());
            }
            dibujarTotales(pagina.stream, factura, pagina.y);
            cerrarPagina(pagina);

            if (factura.getCodCredito() != null && tabla != null && !tabla.isEmpty()) {
                dibujarTablaAmortizacion(documento, factura, CuotaAmortizacion.conFilaInicial(tabla, factura.getTotal()));
            }
            documento.save(salida);
            return salida.toByteArray();
        }
    }

    private static Pagina nuevaPagina(PDDocument documento, Factura factura, String subtitulo) throws IOException {
        PDPage hoja = new PDPage(PDRectangle.A4);
        documento.addPage(hoja);
        PDPageContentStream stream = new PDPageContentStream(documento, hoja);

        stream.setNonStrokingColor(AZUL);
        stream.addRect(0, ALTO - 92, ANCHO, 92);
        stream.fill();
        texto(stream, "MONSTER", MARGEN, ALTO - 43, NEGRITA, 17, Color.WHITE);
        texto(stream, "Comercializadora de celulares", MARGEN, ALTO - 64, NORMAL, 9, new Color(218, 227, 239));
        texto(stream, "FACTURA", ANCHO - MARGEN - 188, ALTO - 40,
                NEGRITA, 10, new Color(218, 227, 239));
        texto(stream, "N.° " + factura.getCodFactura(), ANCHO - MARGEN - 188, ALTO - 65,
                NEGRITA, 20, Color.WHITE);

        float y = ALTO - 121;
        if (subtitulo == null) {
            y = dibujarDatosCliente(stream, factura, y);
        } else {
            texto(stream, subtitulo, MARGEN, y,
                    NEGRITA, 11, AZUL);
            y -= 22;
        }
        return new Pagina(stream, y);
    }

    private static float dibujarDatosCliente(PDPageContentStream stream, Factura factura, float y) throws IOException {
        float alto = 78;
        stream.setNonStrokingColor(new Color(243, 246, 250));
        stream.addRect(MARGEN, y - alto, ANCHO_CONTENIDO, alto);
        stream.fill();
        texto(stream, "CLIENTE", MARGEN + 14, y - 19, NEGRITA, 8, GRIS);
        texto(stream, "FECHA", MARGEN + 330, y - 19, NEGRITA, 8, GRIS);
        texto(stream, texto(factura.getNombre()).isBlank() ? "Consumidor final" : texto(factura.getNombre()),
                MARGEN + 14, y - 39, NEGRITA, 11, AZUL);
        texto(stream, "Cédula: " + texto(factura.getCedula()), MARGEN + 14, y - 58, NORMAL, 9, GRIS);
        texto(stream, fecha(factura.getFecha()), MARGEN + 330, y - 39, NEGRITA, 10, AZUL);
        texto(stream, formaPago(factura.getFormaPago()), MARGEN + 330, y - 58, NORMAL, 9, GRIS);
        return y - alto - 21;
    }

    private static float dibujarEncabezadoTabla(PDPageContentStream stream, float y) throws IOException {
        float alto = 29;
        stream.setNonStrokingColor(AZUL);
        stream.addRect(MARGEN, y - alto, ANCHO_CONTENIDO, alto);
        stream.fill();
        texto(stream, "CÓD.", MARGEN + 10, y - 19, NEGRITA, 8, Color.WHITE);
        texto(stream, "PRODUCTO", MARGEN + 62, y - 19, NEGRITA, 8, Color.WHITE);
        texto(stream, "CANT.", MARGEN + 313, y - 19, NEGRITA, 8, Color.WHITE);
        texto(stream, "P. UNITARIO", MARGEN + 357, y - 19, NEGRITA, 8, Color.WHITE);
        texto(stream, "SUBTOTAL", MARGEN + 448, y - 19, NEGRITA, 8, Color.WHITE);
        return y - alto;
    }

    private static void dibujarFila(PDPageContentStream stream, float y, float alto, DetalleFactura detalle,
            List<String> producto, int indice) throws IOException {
        float base = y - alto;
        stream.setNonStrokingColor(indice % 2 == 0 ? Color.WHITE : FILA);
        stream.addRect(MARGEN, base, ANCHO_CONTENIDO, alto);
        stream.fill();
        stream.setStrokingColor(BORDE);
        stream.moveTo(MARGEN, base);
        stream.lineTo(MARGEN + ANCHO_CONTENIDO, base);
        stream.stroke();

        float textoY = y - 19;
        texto(stream, String.valueOf(detalle.getCodCelular() == null ? "-" : detalle.getCodCelular()),
                MARGEN + 10, textoY, NORMAL, 9, AZUL);
        for (String linea : producto) {
            texto(stream, linea, MARGEN + 62, textoY, NORMAL, 9, AZUL);
            textoY -= 12;
        }
        alinearDerecha(stream, String.valueOf(detalle.getCantidad() == null ? 0 : detalle.getCantidad()),
                MARGEN + 357, textoY + producto.size() * 12, NORMAL, 9, AZUL);
        alinearDerecha(stream, dinero(detalle.getPrecioUnitario()), MARGEN + 442, textoY + producto.size() * 12,
                NORMAL, 9, AZUL);
        alinearDerecha(stream, dinero(detalle.getSubtotal()), MARGEN + ANCHO_CONTENIDO - 10,
                textoY + producto.size() * 12, NEGRITA, 9, AZUL);
    }

    private static void dibujarTotales(PDPageContentStream stream, Factura factura, float y) throws IOException {
        float x = ANCHO - MARGEN - 218;
        float ancho = 218;
        texto(stream, "Subtotal", x + 12, y - 8, NORMAL, 9, GRIS);
        alinearDerecha(stream, dinero(factura.getSubtotal()), x + ancho - 12, y - 8, NORMAL, 9, AZUL);
        String etiquetaDescuento = "EFECTIVO".equals(factura.getFormaPago())
                ? "Descuento efectivo (42%)" : "Descuento";
        texto(stream, etiquetaDescuento, x + 12, y - 28, NORMAL, 9, GRIS);
        String descuento = factura.getDescuento() == null || factura.getDescuento().signum() == 0
                ? dinero(factura.getDescuento()) : "- " + dinero(factura.getDescuento());
        alinearDerecha(stream, descuento, x + ancho - 12, y - 28, NORMAL, 9, AZUL);

        stream.setNonStrokingColor(AZUL_CLARO);
        stream.addRect(x, y - 72, ancho, 31);
        stream.fill();
        texto(stream, "TOTAL", x + 12, y - 61, NEGRITA, 10, AZUL);
        alinearDerecha(stream, dinero(factura.getTotal()), x + ancho - 12, y - 62, NEGRITA, 13, AZUL);

        if ("CREDITO".equals(factura.getFormaPago()) && factura.getNumeroCuotas() != null) {
            texto(stream, "Crédito directo · " + factura.getNumeroCuotas() + " cuotas de "
                    + dinero(factura.getValorCuota()), MARGEN, y - 93, NORMAL, 9, GRIS);
        }
    }

    private static void dibujarTablaAmortizacion(PDDocument documento, Factura factura,
            List<CuotaAmortizacion> cuotas) throws IOException {
        Pagina pagina = nuevaPagina(documento, factura,
                "Tabla de amortización · Crédito N.° " + factura.getCodCredito());
        pagina.y = dibujarEncabezadoAmortizacion(pagina.stream, pagina.y);
        for (CuotaAmortizacion cuota : cuotas) {
            float alto = 27;
            if (pagina.y - alto < 60) {
                cerrarPagina(pagina);
                pagina = nuevaPagina(documento, factura, "Tabla de amortización · continuación");
                pagina.y = dibujarEncabezadoAmortizacion(pagina.stream, pagina.y);
            }
            dibujarFilaAmortizacion(pagina.stream, pagina.y, alto, cuota, cuota.getNumeroCuota());
            pagina.y -= alto;
        }
        cerrarPagina(pagina);
    }

    private static float dibujarEncabezadoAmortizacion(PDPageContentStream stream, float y) throws IOException {
        float alto = 30;
        stream.setNonStrokingColor(AZUL);
        stream.addRect(MARGEN, y - alto, ANCHO_CONTENIDO, alto);
        stream.fill();
        texto(stream, "# CUOTA", MARGEN + 8, y - 20, NEGRITA, 8, Color.WHITE);
        texto(stream, "VALOR CUOTA", MARGEN + 64, y - 20, NEGRITA, 8, Color.WHITE);
        texto(stream, "INTERÉS PAGADO", MARGEN + 158, y - 20, NEGRITA, 8, Color.WHITE);
        texto(stream, "CAPITAL PAGADO", MARGEN + 269, y - 20, NEGRITA, 8, Color.WHITE);
        texto(stream, "SALDO", MARGEN + 424, y - 20, NEGRITA, 8, Color.WHITE);
        return y - alto;
    }

    private static void dibujarFilaAmortizacion(PDPageContentStream stream, float y, float alto,
            CuotaAmortizacion cuota, int indice) throws IOException {
        float base = y - alto;
        stream.setNonStrokingColor(indice % 2 == 0 ? Color.WHITE : FILA);
        stream.addRect(MARGEN, base, ANCHO_CONTENIDO, alto);
        stream.fill();
        stream.setStrokingColor(BORDE);
        stream.moveTo(MARGEN, base);
        stream.lineTo(MARGEN + ANCHO_CONTENIDO, base);
        stream.stroke();

        float textoY = y - 18;
        alinearDerecha(stream, String.valueOf(cuota.getNumeroCuota()), MARGEN + 55, textoY, NORMAL, 9, AZUL);
        alinearDerecha(stream, dineroOpcional(cuota.getValorCuota()), MARGEN + 150, textoY, NORMAL, 9, AZUL);
        alinearDerecha(stream, dineroOpcional(cuota.getInteresPagado()), MARGEN + 257, textoY, NORMAL, 9, AZUL);
        alinearDerecha(stream, dineroOpcional(cuota.getCapitalPagado()), MARGEN + 364, textoY, NORMAL, 9, AZUL);
        alinearDerecha(stream, dineroOpcional(cuota.getSaldo()), MARGEN + ANCHO_CONTENIDO - 8, textoY, NORMAL, 9, AZUL);
    }

    private static void cerrarPagina(Pagina pagina) throws IOException {
        pagina.stream.setStrokingColor(BORDE);
        pagina.stream.moveTo(MARGEN, 34);
        pagina.stream.lineTo(ANCHO - MARGEN, 34);
        pagina.stream.stroke();
        texto(pagina.stream, "Gracias por su compra", MARGEN, 21, NORMAL, 8, GRIS);
        texto(pagina.stream, "MONSTER · Comercializadora de celulares", ANCHO - MARGEN - 190, 21,
                NORMAL, 8, GRIS);
        pagina.stream.close();
    }

    private static void texto(PDPageContentStream stream, String valor, float x, float y,
            PDType1Font fuente, float tamano, Color color) throws IOException {
        stream.beginText();
        stream.setFont(fuente, tamano);
        stream.setNonStrokingColor(color);
        stream.newLineAtOffset(x, y);
        stream.showText(pdfSeguro(valor));
        stream.endText();
    }

    private static void alinearDerecha(PDPageContentStream stream, String valor, float bordeDerecho, float y,
            PDType1Font fuente, float tamano, Color color) throws IOException {
        String seguro = pdfSeguro(valor);
        float ancho = fuente.getStringWidth(seguro) / 1000 * tamano;
        texto(stream, seguro, bordeDerecho - ancho, y, fuente, tamano, color);
    }

    private static List<String> envolver(String valor, float maximo, float tamano) throws IOException {
        String seguro = pdfSeguro(valor).trim();
        if (seguro.isEmpty()) return List.of("-");
        List<String> lineas = new ArrayList<>();
        StringBuilder linea = new StringBuilder();
        for (String palabra : seguro.split("\\s+")) {
            String candidata = linea.isEmpty() ? palabra : linea + " " + palabra;
            if (NORMAL.getStringWidth(candidata) / 1000 * tamano > maximo && !linea.isEmpty()) {
                lineas.add(linea.toString());
                linea.setLength(0);
                linea.append(palabra);
            } else {
                if (!linea.isEmpty()) linea.append(' ');
                linea.append(palabra);
            }
        }
        if (!linea.isEmpty()) lineas.add(linea.toString());
        return lineas;
    }

    private static String pdfSeguro(String valor) {
        if (valor == null || valor.isBlank()) return "-";
        String limpio = Normalizer.normalize(valor, Normalizer.Form.NFC)
                .replace('\u00a0', ' ').replace('\u2013', '-').replace('\u2014', '-').replace('\u2022', '|');
        StringBuilder resultado = new StringBuilder();
        limpio.codePoints().forEach(cp -> {
            if (cp >= 32 && cp <= 255 && !(cp >= 127 && cp <= 159)) resultado.appendCodePoint(cp);
            else if (cp == 0x20AC || cp == 0x201A || cp == 0x201E || cp == 0x2026 || cp == 0x2030
                    || cp == 0x2039 || cp == 0x203A || cp == 0x2122) resultado.appendCodePoint(cp);
            else if (Character.isWhitespace(cp)) resultado.append(' ');
            else resultado.append('?');
        });
        return resultado.toString();
    }

    private static String dinero(BigDecimal valor) {
        return valor == null ? "$0.00" : String.format(Locale.US, "$%,.2f", valor);
    }

    private static String dineroOpcional(BigDecimal valor) {
        return valor == null ? "" : dinero(valor);
    }

    private static String texto(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String fecha(String valor) {
        if (valor == null || valor.isBlank()) return "-";
        String texto = valor.replace('T', ' ');
        return texto.length() > 16 ? texto.substring(0, 16) : texto;
    }

    private static String formaPago(String formaPago) {
        if ("CREDITO".equals(formaPago)) return "Crédito directo";
        if ("EFECTIVO".equals(formaPago)) return "Efectivo";
        return texto(formaPago).isBlank() ? "-" : texto(formaPago);
    }

    private static final class Pagina {
        private final PDPageContentStream stream;
        private float y;

        private Pagina(PDPageContentStream stream, float y) {
            this.stream = stream;
            this.y = y;
        }
    }
}
