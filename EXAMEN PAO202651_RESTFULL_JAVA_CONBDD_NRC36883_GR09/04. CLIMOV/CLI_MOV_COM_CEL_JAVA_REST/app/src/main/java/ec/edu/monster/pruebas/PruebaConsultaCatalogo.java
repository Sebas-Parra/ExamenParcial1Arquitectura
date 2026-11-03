package ec.edu.monster.pruebas;

import ec.edu.monster.servicio.ClienteRest;
import ec.edu.monster.modelo.RespuestaRest;
import java.math.BigDecimal;

final class PruebaConsultaCatalogo {
    static void ejecutar() throws Exception {
        try (SoportePruebasRest servidor = new SoportePruebasRest()) {
            servidor.responder("{\"estado\":\"EXITO\",\"codigo\":\"OK\",\"mensaje\":\"Catalogo de celulares\",\"celulares\":["
                + "{\"codCelular\":1,\"marca\":\"Samsung\",\"modelo\":\"Galaxy A15\",\"precio\":199.00,\"disponible\":true},"
                + "{\"codCelular\":2,\"marca\":\"Xiaomi\",\"modelo\":\"Redmi Note 13\",\"precio\":249.00,\"disponible\":true}]}");
            ClienteRest cliente = new ClienteRest(servidor.endpoint());
            RespuestaRest respuesta = cliente.listarCelulares(false);
            SoportePruebasRest.exigir(respuesta.isExito() && respuesta.getCelulares().size() == 2,
                "Debe deserializar el catalogo de celulares");
            SoportePruebasRest.exigir(new BigDecimal("199.00").compareTo(respuesta.getCelulares().get(0).getPrecio()) == 0
                && "Galaxy A15".equals(respuesta.getCelulares().get(0).getModelo()), "Debe leer precio y modelo");
            SoportePruebasRest.exigir("GET".equals(servidor.metodo) && servidor.ruta.equals("/api/phones"),
                "El catalogo debe consultarse con GET /phones");

            cliente.listarCelulares(true);
            SoportePruebasRest.exigir(servidor.ruta.equals("/api/phones?todos=true"),
                "Con retirados debe enviar ?todos=true");
        }
    }
}
