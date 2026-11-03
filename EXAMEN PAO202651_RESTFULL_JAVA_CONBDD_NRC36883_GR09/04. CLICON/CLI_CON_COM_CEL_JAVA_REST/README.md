# Cliente de consola Java de la Comercializadora de Teléfonos Celulares

Aplicación Java 21 que consume el servidor `WS_COM_CEL_JAVA_REST` mediante HTTP y JSON. No accede al Banco BanQuito directamente: la comercializadora es la que invoca al banco para el crédito directo y la tabla de amortización.

## Funciones

- Consultar el catálogo de celulares.
- Registrar, actualizar y retirar celulares del catálogo.
- Facturar una venta de uno o varios celulares:
  - **Efectivo**, con 42% de descuento (se muestra el total antes de confirmar).
  - **Crédito directo BanQuito**, de 3 a 18 cuotas. Si se aprueba, muestra la factura y la tabla de amortización; si no, el motivo del banco o el monto máximo aprobado.
- Consultar facturas, todas o por cédula, y ver su detalle.
- Consultar la tabla de amortización de un crédito: se ingresa la cédula y se elige el crédito.

## Requisitos y compilación

Se requiere JDK 21 y Maven. Desde PowerShell en esta carpeta ejecute:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\construir.ps1
```

También puede abrir esta carpeta como proyecto Maven en NetBeans y seleccionar **Clean and Build**. El JAR se genera en `target/CLI_CON_COM_CEL_JAVA_REST.jar`; las dependencias se copian a `target/lib`.

## Ejecución

Con `WS_COM_CEL_JAVA_REST` y `WS_BANQUITO_JAVA_REST` desplegados:

```powershell
java -jar .\target\CLI_CON_COM_CEL_JAVA_REST.jar
```

La URL predeterminada del API es:

```text
http://localhost:8080/WS_COM_CEL_JAVA_REST/api
```

Para otro contexto, puerto o servidor, indique como primer argumento una URL base que termine en `/api`, o use la propiedad JVM `comercializadora.endpoint`:

```powershell
java -jar .\target\CLI_CON_COM_CEL_JAVA_REST.jar http://192.168.1.10:8080/WS_COM_CEL_JAVA_REST/api
java -Dcomercializadora.endpoint=http://192.168.1.10:8080/WS_COM_CEL_JAVA_REST/api -jar .\target\CLI_CON_COM_CEL_JAVA_REST.jar
```

Si se indica tanto argumento como propiedad, se usa el argumento.

## Menú

1. Consultar catálogo de celulares.
2. Registrar celular: marca, modelo, descripción opcional y precio.
3. Actualizar celular: Enter conserva cada valor actual; un celular retirado puede volver a ofrecerse.
4. Retirar celular del catálogo; sus facturas se conservan.
5. Facturar venta: celulares y cantidades, forma de pago, cédula y (en efectivo) nombre o (en crédito) número de cuotas, y confirmación.
6. Consultar facturas: cédula o Enter para todas; elegir una muestra su detalle.
7. Consultar tabla de amortización: cédula del cliente y crédito a consultar.
0. Salir. El fin de entrada también cierra la aplicación.

En las listas, `0` vuelve al menú principal. En una terminal se limpia la pantalla entre secciones y se espera Enter antes de volver al menú; se controla con `-Dcomercializadora.interactivo=true|false`. Los precios aceptan punto o coma decimal. Los errores HTTP/JSON, desconexiones y timeouts se muestran sin cerrar el menú.

## Datos para la defensa

Con `BANQUITO.sql` y `COMERCIALIZADORA.sql` recién cargados:

| Cédula | Cliente | Resultado con crédito directo |
|---|---|---|
| 1712345678 | Ana Lucía Torres | Sujeto de crédito (el ejemplo de 450.00 a 6 cuotas se aprueba) |
| 1723456789 | Carlos Andrés Mena | Sujeto de crédito, hasta su monto máximo |
| 1734567890 | Luis Fernando Paredes | Rechazado: hombre menor de 25 años |
| 1745678901 | María José Castillo | Rechazado: sin depósitos en el último mes |
| 1756789012 | Jorge Esteban Ruiz | Rechazado: ya tiene el crédito 1 activo (su tabla se puede consultar con la opción 7) |
| 1767890123 | Sofía Valentina Vega | Aprobado hasta 225.00; un celular más caro se rechaza por monto máximo |

## Pruebas

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

Compila el cliente y ejecuta `PruebaClienteRest` contra un servidor HTTP local simulado: catálogo, mantenimiento, facturación, consulta de facturas, tabla de amortización y un recorrido completo por los menús. No necesita Payara ni base de datos.
