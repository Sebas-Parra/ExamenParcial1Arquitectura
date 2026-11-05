package ec.edu.monster;

import ec.edu.monster.cliente.ClienteRest;
import ec.edu.monster.consola.controlador.ControladorConsola;
import ec.edu.monster.consola.vista.VistaConsola;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;

/** Punto de entrada del cliente de consola de la Comercializadora de Teléfonos Celulares. */
public final class CLI_CON_COM_CEL_JAVA_REST {
    private static final String ENDPOINT_PREDETERMINADO = "https://recycled-facedown-traction.ngrok-free.dev/WS_COM_CEL_JAVA_REST/api";

    private CLI_CON_COM_CEL_JAVA_REST() {
    }

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0]
            : System.getProperty("comercializadora.endpoint", ENDPOINT_PREDETERMINADO);
        try {
            var cliente = new ClienteRest(URI.create(url));
            var salida = new PrintWriter(System.out, true);
            salida.println("Servicio REST: " + url);
            boolean interactiva = Boolean.parseBoolean(System.getProperty("comercializadora.interactivo",
                Boolean.toString(System.console() != null)));
            var vista = new VistaConsola(
                new BufferedReader(new InputStreamReader(System.in)), salida, interactiva);
            new ControladorConsola(cliente, vista).ejecutar();
        } catch (IllegalArgumentException e) {
            System.err.println("URL inválida: " + e.getMessage());
            System.err.println("Uso: java -jar CLI_CON_COM_CEL_JAVA_REST.jar URL_BASE_DEL_API");
        }
    }
}
