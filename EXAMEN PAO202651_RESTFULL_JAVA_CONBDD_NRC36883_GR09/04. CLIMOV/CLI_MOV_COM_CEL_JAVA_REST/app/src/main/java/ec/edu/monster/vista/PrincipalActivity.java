package ec.edu.monster.vista;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import ec.edu.monster.R;
import ec.edu.monster.controlador.ControladorCatalogo;
import ec.edu.monster.controlador.ControladorConsultas;
import ec.edu.monster.controlador.ControladorFacturacion;
import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.servicio.EjecutorAndroid;

/** Pantalla principal: navegación inferior entre catálogo, facturación, facturas y tabla de amortización. */
public final class PrincipalActivity extends AppCompatActivity {
    private static final String PREFERENCIAS = "configuracion";
    private static final String CLAVE_ENDPOINT = "endpoint";

    private final EjecutorAndroid ejecutor = new EjecutorAndroid();
    private PantallaCatalogo catalogo;
    private PantallaFacturacion facturacion;
    private PantallaConsultas consultas;
    private ControladorCatalogo controladorCatalogo;
    private ControladorFacturacion controladorFacturacion;
    private TextView titulo;
    private View progreso;
    private int llamadasEnCurso;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        EdgeToEdge.enable(this,
                SystemBarStyle.dark(Color.TRANSPARENT),
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT));
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_principal);
        AjustesVentana.aplicarMargenes(findViewById(R.id.raiz), findViewById(R.id.cabecera));
        titulo = findViewById(R.id.titulo);
        progreso = findViewById(R.id.progreso);

        ClienteRest cliente = crearCliente();
        catalogo = new PantallaCatalogo(this);
        facturacion = new PantallaFacturacion(this);
        consultas = new PantallaConsultas(this);
        controladorCatalogo = new ControladorCatalogo(cliente, ejecutor, catalogo);
        controladorFacturacion = new ControladorFacturacion(cliente, ejecutor, facturacion);
        catalogo.conectar(controladorCatalogo);
        facturacion.conectar(controladorFacturacion);
        consultas.conectar(new ControladorConsultas(cliente, ejecutor, consultas));

        FrameLayout contenedor = findViewById(R.id.contenedor);
        for (View pantalla : new View[] {catalogo.vista(), facturacion.vista(), consultas.vistaFacturas(),
                consultas.vistaAmortizacion()}) {
            pantalla.setVisibility(View.GONE);
            contenedor.addView(pantalla);
        }

        findViewById(R.id.servidor).setOnClickListener(v -> configurarServidor());
        BottomNavigationView navegacion = findViewById(R.id.navegacion);
        navegacion.setOnItemSelectedListener(item -> {
            mostrar(item.getItemId());
            return true;
        });
        navegacion.setSelectedItemId(R.id.navCatalogo);
    }

    /** Cambia de sección y recarga sus datos para reflejar cambios hechos desde otros clientes. */
    private void mostrar(int seccion) {
        catalogo.vista().setVisibility(seccion == R.id.navCatalogo ? View.VISIBLE : View.GONE);
        facturacion.vista().setVisibility(seccion == R.id.navFacturar ? View.VISIBLE : View.GONE);
        consultas.vistaFacturas().setVisibility(seccion == R.id.navFacturas ? View.VISIBLE : View.GONE);
        consultas.vistaAmortizacion().setVisibility(seccion == R.id.navAmortizacion ? View.VISIBLE : View.GONE);
        if (seccion == R.id.navCatalogo) {
            titulo.setText(R.string.titulo_catalogo);
            controladorCatalogo.cargar();
        } else if (seccion == R.id.navFacturar) {
            titulo.setText(R.string.titulo_facturar);
            controladorFacturacion.cargarCelulares();
        } else if (seccion == R.id.navFacturas) {
            titulo.setText(R.string.titulo_facturas);
            consultas.cargarFacturas();
        } else {
            titulo.setText(R.string.titulo_amortizacion);
        }
    }

    /** Usa la URL guardada desde el botón Servidor o, si no hay, la de config.xml. */
    private ClienteRest crearCliente() {
        String guardada = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE).getString(CLAVE_ENDPOINT, null);
        try {
            if (guardada != null) return new ClienteRest(guardada);
        } catch (IllegalArgumentException ignorada) {
            // Una URL guardada inválida no debe impedir abrir la app; se usa la predeterminada.
        }
        return new ClienteRest(getString(R.string.endpoint_rest));
    }

    private void configurarServidor() {
        View vista = getLayoutInflater().inflate(R.layout.dialogo_servidor, null);
        EditText url = vista.findViewById(R.id.url);
        SharedPreferences preferencias = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE);
        url.setText(preferencias.getString(CLAVE_ENDPOINT, getString(R.string.endpoint_rest)));
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.servidor)
            .setView(vista)
            .setNegativeButton(R.string.cancelar, null)
            .setNeutralButton("Predeterminada", (d, b) -> {
                preferencias.edit().remove(CLAVE_ENDPOINT).apply();
                recreate();
            })
            .setPositiveButton(R.string.guardar, (d, b) -> {
                try {
                    String endpoint = ClienteRest.normalizarEndpoint(url.getText().toString());
                    preferencias.edit().putString(CLAVE_ENDPOINT, endpoint).apply();
                    recreate();
                } catch (IllegalArgumentException ex) {
                    mostrarAviso("URL inválida", ex.getMessage());
                }
            })
            .show();
    }

    /** Muestra la barra de progreso mientras haya alguna llamada al servidor en curso. */
    void setOcupado(boolean ocupado) {
        llamadasEnCurso = Math.max(0, llamadasEnCurso + (ocupado ? 1 : -1));
        progreso.setVisibility(llamadasEnCurso > 0 ? View.VISIBLE : View.GONE);
    }

    void mostrarAviso(String tituloAviso, String mensaje) {
        if (isFinishing() || isDestroyed()) return;
        new MaterialAlertDialogBuilder(this)
            .setTitle(tituloAviso)
            .setMessage(mensaje)
            .setPositiveButton(R.string.aceptar, null)
            .show();
    }

    /** Texto gris para listas vacías. */
    TextView textoVacio(int texto) {
        TextView vacio = new TextView(this);
        vacio.setText(texto);
        vacio.setTextColor(ContextCompat.getColor(this, R.color.gris));
        int margen = (int) (16 * getResources().getDisplayMetrics().density);
        vacio.setPadding(0, margen, 0, margen);
        return vacio;
    }

    @Override
    protected void onDestroy() {
        ejecutor.cerrar();
        super.onDestroy();
    }
}
