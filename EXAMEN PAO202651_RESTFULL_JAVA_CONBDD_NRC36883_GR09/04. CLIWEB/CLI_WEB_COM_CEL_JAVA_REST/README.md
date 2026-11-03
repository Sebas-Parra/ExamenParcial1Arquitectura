# Cliente Web REST de la Comercializadora de Teléfonos Celulares

Aplicación web Java 21 empaquetada como WAR. Sigue MVC con servlets controladores, vistas JSP y un cliente HTTP/JSON que consume el servidor REST `04. SERVIDOR/WS_COM_CEL_JAVA_REST`. No accede al Banco BanQuito directamente: la comercializadora es la que invoca al banco para el crédito directo y la tabla de amortización.

## Requisitos

- JDK 21 y Maven 3.8 o superior.
- Payara o un servidor Jakarta EE 11 compatible.
- `WS_COM_CEL_JAVA_REST` y `WS_BANQUITO_JAVA_REST` desplegados, con `COMERCIALIZADORA.sql` y `BANQUITO.sql` cargados.

## Compilar y desplegar

Desde esta carpeta, ejecutar `mvn package`. Maven genera `target/CLI_WEB_COM_CEL_JAVA_REST-1.0-SNAPSHOT.war` (incluye Gson); despliega ese archivo en Payara y abre `http://localhost:8080/CLI_WEB_COM_CEL_JAVA_REST-1.0-SNAPSHOT/` o el contexto asignado al WAR.

El endpoint REST predeterminado se configura en `src/main/webapp/WEB-INF/web.xml` como `rest.endpoint`:

```text
http://localhost:8080/WS_COM_CEL_JAVA_REST/api
```

Para otro contexto o servidor, cambia ese parámetro o define la propiedad JVM `-Dcomercializadora.rest.endpoint=http://host:8080/WS_COM_CEL_JAVA_REST/api`. La propiedad se usa si `rest.endpoint` no está configurado.

## Funciones

Un menú lateral (o superior en pantallas angostas) da acceso a cuatro páginas:

- **Catálogo** (`/catalogo`): lista de celulares con formulario para registrar y editar; *Retirar* lo quita de la venta sin borrar sus facturas y *Mostrar retirados* permite volver a ofrecerlo desde Editar.
- **Facturar venta** (`/facturar`): carrito guardado en la sesión, forma de pago (efectivo con 42% de descuento o crédito directo de 3 a 18 cuotas) y datos del comprador. Los totales de cada forma de pago se calculan antes de enviar. Si la factura se registra se abre su detalle; si el banco la rechaza se muestra el motivo o el monto máximo aprobado.
- **Facturas** (`/facturas`): todas o por cédula; *Ver detalle* muestra la factura y, si es a crédito, su tabla de amortización consultada en el banco.
- **Tabla de amortización** (`/amortizacion`): se ingresa la cédula, se listan los créditos del cliente y al elegir uno se muestra su tabla (# Cuota, Valor Cuota, Interés Pagado, Capital Pagado, Saldo).

Los formularios usan POST con token CSRF (`FiltroCsrf`) y, tras cada operación correcta, redirigen y muestran un aviso (patrón POST-redirect-GET), así que recargar la página no repite la venta.

## Estructura MVC

- `ec.edu.monster.controlador`: servlets de catálogo, facturación, facturas y amortización; `ControladorBase` con avisos, errores y redirecciones; filtro CSRF.
- `ec.edu.monster.vista`: escape y formato para las JSP de `WEB-INF/vista` (con `encabezado.jspf`, `pie.jspf` y `tabla-amortizacion.jspf` compartidos).
- `ec.edu.monster.modelo`: celulares, facturas, créditos, carrito de venta y respuestas REST.
- `ec.edu.monster.servicio`: cliente HTTP/JSON y lectura del endpoint configurado.

## Pruebas

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

Ejecuta `PruebaClienteRest` contra un servidor HTTP local simulado (catálogo, mantenimiento, facturación, facturas, detalle de factura y tabla de amortización) y prueba los cálculos del carrito. No necesita Payara ni base de datos.
