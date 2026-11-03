# Cliente móvil Android de la Comercializadora de Teléfonos Celulares

Aplicación Android en Java con arquitectura MVC. Consume el servidor REST `WS_COM_CEL_JAVA_REST` mediante HTTP y JSON con `HttpURLConnection` y `org.json`, que ya vienen en Android. No accede al Banco BanQuito directamente: la comercializadora es la que invoca al banco para el crédito directo y la tabla de amortización.

## Requisitos

- Android Studio con JDK 21 y el SDK de Android 35.
- Dispositivo o emulador con Android 8.0 (API 26) o superior.
- `WS_COM_CEL_JAVA_REST` y `WS_BANQUITO_JAVA_REST` desplegados en Payara, con `COMERCIALIZADORA.sql` y `BANQUITO.sql` cargados.

## Servidor

La URL base del API está en `app/src/main/res/values/config.xml`:

```text
http://10.0.2.2:8080/WS_COM_CEL_JAVA_REST/api
```

`10.0.2.2` es el `localhost` del computador visto desde el emulador. En un celular físico use la IP del computador (`ipconfig`) en la misma red o una URL de ngrok. También se puede cambiar sin recompilar con el botón **Servidor** de la cabecera; la URL queda guardada en el dispositivo y **Predeterminada** vuelve a la de `config.xml`.

## Compilar y ejecutar

Abra esta carpeta en Android Studio y ejecute la configuración `app`. Desde la línea de comandos:

```powershell
.\gradlew.bat :app:assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/ComercializadoraMonsterREST-debug.apk`.

## Funciones

Una barra inferior da acceso a cuatro secciones; cada una recarga sus datos al abrirse.

- **Catálogo:** tarjetas con los celulares; *Nuevo celular* y *Editar* abren un formulario, *Retirar* pide confirmación y conserva las facturas. *Mostrar retirados* permite volver a ofrecer un celular desde Editar.
- **Facturar:** se eligen celulares y cantidades para el carrito, la forma de pago (efectivo con 42% de descuento o crédito directo de 3 a 18 cuotas) y la cédula; en efectivo también el nombre. Los totales se actualizan al momento y se pide confirmación antes de enviar. Si se aprueba, se abre la factura con su tabla de amortización; si el banco la rechaza, se muestra el motivo o el monto máximo aprobado.
- **Facturas:** todas o por cédula; al tocar una se abre su detalle y, si es a crédito, su tabla de amortización consultada en el banco.
- **Amortización:** se ingresa la cédula, se listan los créditos del cliente y al tocar uno se muestra su tabla (#, Cuota, Interés, Capital, Saldo).

## Estructura MVC

- `ec.edu.monster.vista`: actividad principal, pantallas de cada sección, diálogos de celular y factura, e interfaces que usan los controladores.
- `ec.edu.monster.controlador`: catálogo, facturación y consultas. No dependen de Android: usan las interfaces de vista y `EjecutorTareas`, así que se prueban fuera del teléfono.
- `ec.edu.monster.modelo`: celulares, facturas, créditos, carrito de venta y respuestas REST.
- `ec.edu.monster.servicio`: cliente HTTP/JSON y ejecución de las llamadas en segundo plano.

## Pruebas

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

Ejecuta, como prueba unitaria de JUnit, `PruebaClienteRest` contra un servidor HTTP local simulado: catálogo, mantenimiento, facturación, facturas, detalle de factura, tabla de amortización, cálculos del carrito y el flujo del controlador de facturación con una vista falsa. No necesita emulador ni Payara.
