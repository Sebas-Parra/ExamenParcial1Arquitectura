# CLI_ESC_COM_CEL_JAVA_REST

Aplicación de escritorio Swing con arquitectura MVC. Consume el servidor REST de la Comercializadora de Teléfonos Celulares (`WS_COM_CEL_JAVA_REST`) mediante HTTP y JSON. No accede al Banco BanQuito directamente: la comercializadora es la que invoca al banco para el crédito directo y la tabla de amortización.

## Requisitos

- JDK 21
- Maven (también puedes usar el Maven incluido con NetBeans)
- `WS_COM_CEL_JAVA_REST` y `WS_BANQUITO_JAVA_REST` desplegados, con `COMERCIALIZADORA.sql` y `BANQUITO.sql` cargados

## Abrir y ejecutar en NetBeans

Abre **File > Open Project** y selecciona `04. CLIESC/CLI_ESC_COM_CEL_JAVA_REST`. Ejecuta la aplicación Maven con `ec.edu.monster.ClienteEscritorio` como clase principal. El empaquetado genera `target/CLI_ESC_COM_CEL_JAVA_REST.jar` y copia Gson a `target/lib`.

También puedes compilar desde esta carpeta:

```powershell
mvn -DskipTests package
```

Para iniciar el JAR, conserva el directorio `target/lib` junto al archivo:

```powershell
java -jar target/CLI_ESC_COM_CEL_JAVA_REST.jar
```

## Servidor y endpoint

El endpoint predeterminado es:

```text
http://localhost:8080/WS_COM_CEL_JAVA_REST/api
```

Puedes sustituirlo con la propiedad JVM `comercializadora.endpoint` o con el primer argumento de la aplicación:

```powershell
java -Dcomercializadora.endpoint=http://192.168.1.10:8080/WS_COM_CEL_JAVA_REST/api -jar target/CLI_ESC_COM_CEL_JAVA_REST.jar
```

La URL en uso se muestra en la esquina inferior derecha de la ventana.

## Funciones

La ventana tiene una navegación lateral con cuatro secciones; cada una recarga sus datos al abrirse.

- **Catálogo:** lista de celulares con registro, edición (doble clic) y retiro. *Mostrar retirados* permite volver a ofrecer un celular desde Editar.
- **Facturar venta:** se agregan celulares y cantidades al carrito, se elige la forma de pago y se ingresan los datos del comprador. El subtotal, el descuento del 42% (efectivo) y el total se actualizan al momento. En crédito directo se elige el plazo (3 a 18 cuotas) y el nombre se toma del banco. Si el banco aprueba, se abre la factura con su tabla de amortización; si no, se muestra el motivo o el monto máximo aprobado.
- **Facturas:** todas o por cédula. *Ver detalle* (o doble clic) abre la factura; si es a crédito, también consulta y muestra su tabla de amortización.
- **Tabla de amortización:** se ingresa la cédula, se listan los créditos del cliente en el banco y al seleccionar uno se muestra su tabla (# Cuota, Valor Cuota, Interés Pagado, Capital Pagado, Saldo).

## Estructura MVC

- `ec.edu.monster.vista`: ventana principal, paneles de cada sección, diálogos de celular y factura, estilos y modelo de tabla
- `ec.edu.monster.controlador`: catálogo, facturación, consultas y navegación; `TareaRest` ejecuta las llamadas en `SwingWorker` y muestra los errores
- `ec.edu.monster.modelo`: celulares, facturas, créditos, carrito de venta y respuestas REST
- `ec.edu.monster.servicio`: cliente HTTP/JSON

Mientras hay una llamada en curso la ventana muestra el cursor de espera y no acepta clics.

## Pruebas

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

Ejecuta `PruebaClienteRest` contra un servidor HTTP local simulado (catálogo, mantenimiento, facturación, consulta de facturas y tabla de amortización) y prueba los cálculos del carrito. No necesita Payara ni base de datos.
