package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.modelo.Celular;
import ec.edu.monster.modelo.RespuestaRest;
import java.math.BigDecimal;
import java.net.URI;

final class PruebaMantenimientoCelular {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            ClienteRest cliente = new ClienteRest(URI.create(servidor.endpoint()));
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Celular registrado\","
                + "\"celular\":{\"codCelular\":9,\"marca\":\"Nokia\",\"modelo\":\"G42\",\"precio\":159.90,\"disponible\":true}}");
            RespuestaRest creado = cliente.crearCelular(new Celular("Nokia", "G42", null, new BigDecimal("159.90")));
            SoportePruebasRest.exigir(creado.isExito() && creado.getCelular().getCodCelular() == 9,
                "Crear debe devolver el celular con su codigo");
            SoportePruebasRest.exigir("POST".equals(servidor.metodo) && servidor.ruta.equals("/api/phones")
                && servidor.cuerpo.contains("\"marca\":\"Nokia\"") && servidor.cuerpo.contains("\"precio\":159.90")
                && !servidor.cuerpo.contains("descripcion"), "Crear debe enviar POST /phones sin campos nulos");

            cliente.actualizarCelular(9, new Celular("Nokia", "G42 5G", "128 GB", new BigDecimal("175")));
            SoportePruebasRest.exigir("PUT".equals(servidor.metodo) && servidor.ruta.equals("/api/phones/9")
                && servidor.cuerpo.contains("\"modelo\":\"G42 5G\""), "Actualizar debe enviar PUT /phones/{id}");

            cliente.eliminarCelular(9);
            SoportePruebasRest.exigir("DELETE".equals(servidor.metodo) && servidor.ruta.equals("/api/phones/9"),
                "Retirar debe enviar DELETE /phones/{id}");

            servidor.responder(400, "{\"estado\":\"ERROR\",\"codigo\":\"DATA-001\",\"mensaje\":\"El precio debe ser mayor que cero\"}");
            RespuestaRest error = cliente.crearCelular(new Celular("Nokia", "G42", null, BigDecimal.ONE));
            SoportePruebasRest.exigir(!error.isExito() && "DATA-001".equals(error.getCodigo()) && error.getEstadoHttp() == 400,
                "Debe conservar el codigo y el estado HTTP de los errores");
        }
    }
}
