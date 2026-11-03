package ec.edu.monster.servicio;

import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.Credito;
import ec.edu.monster.modelo.CuotaAmortizacion;
import ec.edu.monster.modelo.DetalleFactura;
import ec.edu.monster.modelo.Factura;
import ec.edu.monster.modelo.FacturaRequest;
import ec.edu.monster.modelo.RespuestaRest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Adaptador HTTP/JSON del API REST de la Comercializadora de Teléfonos Celulares.
 * Usa HttpURLConnection y org.json, que ya vienen en Android; así las vistas y controladores
 * no conocen las rutas, los verbos HTTP ni el JSON.
 */
public final class ClienteRest {
    private static final int TIEMPO_CONEXION_MS = 10_000;
    private static final int TIEMPO_RESPUESTA_MS = 30_000;

    private final String base;

    public ClienteRest(String direccion) {
        this.base = normalizarEndpoint(direccion);
    }

    /** Valida la URL base del API (la que termina en /api) y quita las barras finales. */
    public static String normalizarEndpoint(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("Escribe la URL del API REST.");
        }
        try {
            URI uri = new URI(direccion.trim());
            String esquema = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if (!("http".equals(esquema) || "https".equals(esquema)) || uri.getHost() == null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null) {
                throw new IllegalArgumentException("La URL debe empezar con http:// o https://, incluir el servidor y no tener parámetros.");
            }
            String texto = uri.toString();
            while (texto.endsWith("/")) {
                texto = texto.substring(0, texto.length() - 1);
            }
            return texto;
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("La URL del API no es válida.", ex);
        }
    }

    public String endpoint() {
        return base;
    }

    public RespuestaRest listarCelulares(boolean incluirRetirados) throws IOException {
        return invocar("GET", incluirRetirados ? "phones?todos=true" : "phones", null);
    }

    public RespuestaRest crearCelular(Celular celular) throws IOException {
        return invocar("POST", "phones", json(celular));
    }

    public RespuestaRest actualizarCelular(int codCelular, Celular celular) throws IOException {
        return invocar("PUT", "phones/" + codCelular, json(celular));
    }

    public RespuestaRest eliminarCelular(int codCelular) throws IOException {
        return invocar("DELETE", "phones/" + codCelular, null);
    }

    public RespuestaRest facturar(FacturaRequest factura) throws IOException {
        try {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("cedula", factura.getCedula());
            if (factura.getNombre() != null) cuerpo.put("nombre", factura.getNombre());
            cuerpo.put("formaPago", factura.getFormaPago());
            if (factura.getNumeroCuotas() != null) cuerpo.put("numeroCuotas", factura.getNumeroCuotas().intValue());
            JSONArray detalles = new JSONArray();
            for (DetalleFactura d : factura.getDetalles()) {
                JSONObject linea = new JSONObject();
                linea.put("codCelular", d.getCodCelular().intValue());
                linea.put("cantidad", d.getCantidad().intValue());
                detalles.put(linea);
            }
            cuerpo.put("detalles", detalles);
            return invocar("POST", "invoices", cuerpo);
        } catch (JSONException ex) {
            throw new IOException("No se pudo preparar la factura.", ex);
        }
    }

    public RespuestaRest listarFacturas(String cedula) throws IOException {
        String filtro = cedula == null || cedula.trim().isEmpty() ? "" : "?cedula=" + codificar(cedula.trim());
        return invocar("GET", "invoices" + filtro, null);
    }

    public RespuestaRest obtenerFactura(int codFactura) throws IOException {
        return invocar("GET", "invoices/" + codFactura, null);
    }

    public RespuestaRest listarCreditos(String cedula) throws IOException {
        return invocar("GET", "credits?cedula=" + codificar(cedula == null ? "" : cedula.trim()), null);
    }

    public RespuestaRest obtenerTablaAmortizacion(int codCredito) throws IOException {
        return invocar("GET", "credits/" + codCredito + "/amortization", null);
    }

    private RespuestaRest invocar(String metodo, String ruta, JSONObject cuerpo) throws IOException {
        HttpURLConnection conexion = null;
        try {
            conexion = (HttpURLConnection) url(ruta).openConnection();
            conexion.setConnectTimeout(TIEMPO_CONEXION_MS);
            conexion.setReadTimeout(TIEMPO_RESPUESTA_MS);
            conexion.setRequestMethod(metodo);
            conexion.setUseCaches(false);
            conexion.setRequestProperty("Accept", "application/json");
            if (cuerpo != null) {
                byte[] bytes = cuerpo.toString().getBytes(StandardCharsets.UTF_8);
                conexion.setDoOutput(true);
                conexion.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                try (OutputStream salida = conexion.getOutputStream()) {
                    salida.write(bytes);
                }
            }

            // Los errores de negocio llegan con HTTP 4xx/5xx y el mismo JSON de respuesta.
            int estadoHttp = conexion.getResponseCode();
            InputStream entrada = estadoHttp >= 400 ? conexion.getErrorStream() : conexion.getInputStream();
            String texto = entrada == null ? "" : leer(entrada);
            if (texto.trim().isEmpty()) {
                throw new IOException(mensajeHttp(estadoHttp));
            }
            try {
                return leerRespuesta(new JSONObject(texto), estadoHttp);
            } catch (JSONException ex) {
                throw new IOException(estadoHttp >= 400 ? mensajeHttp(estadoHttp)
                        : "El servidor no respondió con JSON. Revise que la URL termine en /api.", ex);
            }
        } catch (SocketTimeoutException ex) {
            throw new IOException("El servidor tardó demasiado en responder.", ex);
        } catch (ConnectException | UnknownHostException ex) {
            throw new IOException("No se pudo conectar con el servidor. Revise la URL, la red y que Payara esté encendido.", ex);
        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static RespuestaRest leerRespuesta(JSONObject json, int estadoHttp) throws JSONException, IOException {
        RespuestaRest r = new RespuestaRest();
        r.setEstadoHttp(estadoHttp);
        r.setEstado(texto(json, "estado"));
        r.setCodigo(texto(json, "codigo"));
        r.setMensaje(texto(json, "mensaje"));
        if (vacio(r.getCodigo()) || vacio(r.getMensaje())) {
            throw new IOException(estadoHttp >= 400 ? mensajeHttp(estadoHttp)
                    : "El servidor devolvió una respuesta incompleta.");
        }
        r.setNombre(texto(json, "nombre"));
        r.setMotivo(texto(json, "motivo"));
        r.setMontoMaximo(decimal(json, "montoMaximo"));
        r.setCodCredito(entero(json, "codCredito"));
        r.setMonto(decimal(json, "monto"));
        r.setNumeroCuotas(entero(json, "numeroCuotas"));
        r.setTasaAnual(decimal(json, "tasaAnual"));
        if (!json.isNull("celular")) r.setCelular(celular(json.getJSONObject("celular")));
        if (!json.isNull("factura")) r.setFactura(factura(json.getJSONObject("factura")));
        JSONArray arreglo = json.optJSONArray("celulares");
        if (arreglo != null) for (int i = 0; i < arreglo.length(); i++) r.getCelulares().add(celular(arreglo.getJSONObject(i)));
        arreglo = json.optJSONArray("facturas");
        if (arreglo != null) for (int i = 0; i < arreglo.length(); i++) r.getFacturas().add(factura(arreglo.getJSONObject(i)));
        arreglo = json.optJSONArray("creditos");
        if (arreglo != null) for (int i = 0; i < arreglo.length(); i++) r.getCreditos().add(credito(arreglo.getJSONObject(i)));
        arreglo = json.optJSONArray("tablaAmortizacion");
        if (arreglo != null) for (int i = 0; i < arreglo.length(); i++) {
            JSONObject c = arreglo.getJSONObject(i);
            r.getTablaAmortizacion().add(new CuotaAmortizacion(c.getInt("numeroCuota"), decimal(c, "valorCuota"),
                decimal(c, "interesPagado"), decimal(c, "capitalPagado"), decimal(c, "saldo")));
        }
        return r;
    }

    private static Celular celular(JSONObject j) throws JSONException {
        Boolean disponible = j.isNull("disponible") ? null : j.getBoolean("disponible");
        return new Celular(entero(j, "codCelular"), texto(j, "marca"), texto(j, "modelo"), texto(j, "descripcion"),
            decimal(j, "precio"), disponible);
    }

    private static Factura factura(JSONObject j) throws JSONException {
        Factura f = new Factura();
        f.setCodFactura(entero(j, "codFactura"));
        f.setCedula(texto(j, "cedula"));
        f.setNombre(texto(j, "nombre"));
        f.setFecha(texto(j, "fecha"));
        f.setFormaPago(texto(j, "formaPago"));
        f.setSubtotal(decimal(j, "subtotal"));
        f.setDescuento(decimal(j, "descuento"));
        f.setTotal(decimal(j, "total"));
        f.setCodCredito(entero(j, "codCredito"));
        f.setNumeroCuotas(entero(j, "numeroCuotas"));
        f.setValorCuota(decimal(j, "valorCuota"));
        List<DetalleFactura> detalles = new ArrayList<>();
        JSONArray arreglo = j.optJSONArray("detalles");
        if (arreglo != null) for (int i = 0; i < arreglo.length(); i++) {
            JSONObject d = arreglo.getJSONObject(i);
            detalles.add(new DetalleFactura(entero(d, "codCelular"), texto(d, "marca"), texto(d, "modelo"),
                entero(d, "cantidad"), decimal(d, "precioUnitario"), decimal(d, "subtotal")));
        }
        f.setDetalles(detalles);
        return f;
    }

    private static Credito credito(JSONObject j) throws JSONException {
        Integer codCliente = entero(j, "codCliente");
        Integer cuotas = entero(j, "numeroCuotas");
        return new Credito(j.getInt("codCredito"), codCliente == null ? 0 : codCliente, decimal(j, "monto"),
            cuotas == null ? 0 : cuotas, decimal(j, "tasaAnual"), decimal(j, "valorCuota"),
            texto(j, "fecha"), texto(j, "estado"));
    }

    private static JSONObject json(Celular celular) throws IOException {
        try {
            JSONObject json = new JSONObject();
            json.put("marca", celular.getMarca());
            json.put("modelo", celular.getModelo());
            if (celular.getDescripcion() != null) json.put("descripcion", celular.getDescripcion());
            json.put("precio", celular.getPrecio());
            if (celular.getDisponible() != null) json.put("disponible", celular.getDisponible().booleanValue());
            return json;
        } catch (JSONException ex) {
            throw new IOException("No se pudo preparar los datos del celular.", ex);
        }
    }

    /** optString devuelve "null" para JSON null en algunas versiones de Android; se evita aquí. */
    private static String texto(JSONObject json, String nombre) {
        return json.isNull(nombre) ? null : json.optString(nombre, null);
    }

    private static Integer entero(JSONObject json, String nombre) throws JSONException {
        return json.isNull(nombre) ? null : json.getInt(nombre);
    }

    /** Los números llegan como Integer, Long o Double según el valor; se convierten por su texto. */
    private static BigDecimal decimal(JSONObject json, String nombre) throws JSONException {
        return json.isNull(nombre) ? null : new BigDecimal(json.get(nombre).toString());
    }

    private URL url(String ruta) throws IOException {
        try {
            return new URL(base + "/" + ruta);
        } catch (MalformedURLException ex) {
            throw new IOException("La URL del API no es válida.", ex);
        }
    }

    private static String codificar(String valor) throws IOException {
        try {
            return URLEncoder.encode(valor, "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IOException("No se pudo codificar la consulta.", ex);
        }
    }

    private static String leer(InputStream entrada) throws IOException {
        try (InputStream contenido = entrada; ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
            byte[] bloque = new byte[4096];
            int leidos;
            while ((leidos = contenido.read(bloque)) != -1) {
                bytes.write(bloque, 0, leidos);
            }
            return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String mensajeHttp(int estadoHttp) {
        return estadoHttp == 404
                ? "No se encontró el API en esa URL (HTTP 404). Revise el contexto del WAR y que termine en /api."
                : "El servidor respondió con el estado HTTP " + estadoHttp + ".";
    }

    private static boolean vacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }
}
