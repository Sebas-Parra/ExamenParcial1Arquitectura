# Servidor REST de la Comercializadora de Teléfonos Celulares

Aplicación Java 21 empaquetada como WAR. Expone Jakarta RESTful Web Services (JAX-RS) con arquitectura MVC: `PhoneResource`, `InvoiceResource` y `CreditResource` como controladores, `CatalogoServices` y `FacturacionServices` para las reglas de negocio, `RepositorioComercializadoraJdbc` para el acceso a datos y `ClienteBanquitoRest` para invocar los servicios web del Banco BanQuito (`WS_BANQUITO_JAVA_REST`). Usa la base MariaDB/MySQL `comercializadora`.

Es la segunda aplicación web del examen: los clientes de consola, escritorio, móvil y web consumen este servidor, y este servidor consume al banco.

```
Clientes (CLICON, CLIESC, CLIMOV, CLIWEB) ──REST──► WS_COM_CEL_JAVA_REST ──REST──► WS_BANQUITO_JAVA_REST
                                                        │                              │
                                                  comercializadora                  banquito
```

## Base de datos

El script está en `03. BDD/COMERCIALIZADORA.sql`. Crea la base `comercializadora`, el usuario `comercializadora`/`comercializadora`, 8 celulares y dos facturas de ejemplo. Ejecútelo como `root` desde HeidiSQL (Archivo > Cargar archivo SQL > Ejecutar) o, desde la carpeta del examen, en PowerShell:

```powershell
& "C:\Program Files\MariaDB 13.0\bin\mysql.exe" -u root -p -e "source 03. BDD/COMERCIALIZADORA.sql"
```

| Tabla | Contenido |
|---|---|
| `CELULAR` | Catálogo de teléfonos a la venta (sin inventario). `DISPONIBLE = 0` lo retira del catálogo. |
| `COMPRADOR` | Cédula y nombre de quien compra. |
| `FACTURA` | Cabecera: forma de pago, subtotal, descuento, total y, en crédito directo, el crédito del banco, cuotas y valor de cuota. |
| `DETALLE_FACTURA` | Celulares facturados, cantidad y precio. |

La factura 2 de ejemplo es la venta a crédito de Jorge Esteban Ruiz y corresponde al crédito 1 de `BANQUITO.sql`, así que conviene cargar los dos scripts juntos.

## API

La raíz JAX-RS es `/api`. Con el despliegue por defecto en Payara la URL base es `http://localhost:8080/WS_COM_CEL_JAVA_REST/api`.

| Método y ruta | Descripción |
|---|---|
| `GET /api/phones` | Lista los celulares disponibles (`?todos=true` incluye los retirados). |
| `GET /api/phones/{codCelular}` | Consulta un celular. |
| `POST /api/phones` | Registra un celular: `marca`, `modelo`, `descripcion` (opcional), `precio`. |
| `PUT /api/phones/{codCelular}` | Actualiza un celular; `disponible` es opcional y permite volver a ofrecerlo. |
| `DELETE /api/phones/{codCelular}` | Retira el celular del catálogo; sus facturas se conservan. |
| `POST /api/invoices` | Factura en efectivo o con crédito directo. |
| `GET /api/invoices?cedula={cedula}` | Lista las facturas (sin `cedula`, todas). |
| `GET /api/invoices/{codFactura}` | Consulta una factura con su detalle. |
| `GET /api/credits?cedula={cedula}` | Lista, desde el banco, los créditos del comprador. |
| `GET /api/credits/{codCredito}/amortization` | Consulta, desde el banco, la tabla de amortización de un crédito. |

### Facturar en efectivo

```http
POST /WS_COM_CEL_JAVA_REST/api/invoices
Content-Type: application/json

{"cedula":"1712345678","nombre":"Ana Lucía Torres","formaPago":"EFECTIVO",
 "detalles":[{"codCelular":1,"cantidad":1}]}
```

Respuesta resumida: `subtotal` 199.00, `descuento` 83.58 (42%), `total` 115.42.

### Facturar con crédito directo

```http
POST /WS_COM_CEL_JAVA_REST/api/invoices
Content-Type: application/json

{"cedula":"1712345678","formaPago":"CREDITO","numeroCuotas":6,
 "detalles":[{"codCelular":4,"cantidad":1}]}
```

El nombre no hace falta: se toma del banco. Respuesta resumida:

```json
{
  "estado": "EXITO",
  "codigo": "OK",
  "mensaje": "Factura registrada con credito directo",
  "factura": {"codFactura": 3, "formaPago": "CREDITO", "subtotal": 450.00, "descuento": 0.00, "total": 450.00,
              "codCredito": 2, "numeroCuotas": 6, "valorCuota": 78.65, "detalles": [...]},
  "codCliente": 1,
  "montoMaximo": ...,
  "codCredito": 2,
  "tasaAnual": 16.50,
  "tablaAmortizacion": [{"numeroCuota": 1, "valorCuota": 78.65, "interesPagado": 6.19, "capitalPagado": 72.46, "saldo": 377.54}]
}
```

## Reglas de negocio

**Efectivo:** descuento del 42% sobre el subtotal; `total = subtotal − descuento`. No se invoca al banco.

**Crédito directo**, en este orden:
1. `GET /credits/eligibility/{cedula}` en el banco: si no es sujeto de crédito se rechaza con `CRED-001` y el `motivo` del banco.
2. `GET /credits/max-amount/{codCliente}`: si el valor de la compra supera `montoMaximo` se rechaza con `CRED-002`.
3. `POST /credits` con `codCliente`, `precio` (el total de la factura) y `numeroCuotas` (3 a 18): el banco otorga el crédito y crea la tabla de amortización.
4. Se registra la factura con el código del crédito, sin descuento.

La factura solo se guarda si el banco otorgó el crédito. Si el banco lo otorga pero falla el registro local, la respuesta `BD-001` indica el código del crédito otorgado.

## Respuestas y errores

Todas las respuestas usan `estado`, `codigo` y `mensaje`; el resto de los campos se incluye cuando corresponde. Los errores del banco (`CRED-001`, `CRED-002`, `CRED-003`, `CLI-001`) se devuelven con el mismo código.

| Código | HTTP | Significado |
|---|---|---|
| `OK` | 200 | Operación correcta. |
| `DATA-001` | 400 | Datos incompletos o fuera de rango. |
| `CEL-001` | 404 | Celular inexistente. |
| `CEL-002` | 400 | El celular está retirado del catálogo. |
| `FAC-001` | 404 | Factura inexistente. |
| `CRED-001` | 400 | El comprador no es sujeto de crédito (incluye `motivo`). |
| `CRED-002` | 400 | La compra supera el monto máximo (incluye `montoMaximo`). |
| `CRED-003` | 404 | Crédito inexistente en el banco. |
| `BANCO-001` | 502 | No se pudo comunicar con el Banco BanQuito. |
| `BD-001` | 500 | Error de base de datos. |

## Configuración

Propiedades JVM opcionales del servidor:

- `comercializadora.db.url` (por defecto `jdbc:mariadb://localhost:3306/comercializadora`)
- `comercializadora.db.usuario` (por defecto `comercializadora`)
- `comercializadora.db.clave` (por defecto `comercializadora`)
- `comercializadora.banco.url` (por defecto `http://localhost:8080/WS_BANQUITO_JAVA_REST/api`)

El driver de MariaDB va dentro del WAR; no hace falta configurar un pool en Payara.

## Compilación

```powershell
mvn package
```

Despliegue `target/WS_COM_CEL_JAVA_REST-1.0-SNAPSHOT.war` en Payara (Jakarta EE 11), en el mismo servidor que `WS_BANQUITO_JAVA_REST` o en otro ajustando `comercializadora.banco.url`.

## Pruebas

Pruebas locales del catálogo y la facturación con un banco simulado, sin base de datos ni Payara:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1 -SoloServicio
```

Con los dos WAR desplegados y las dos bases recién cargadas:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

La prueba HTTP no otorga créditos, así que puede repetirse sin recargar `BANQUITO.sql`. Cada ejecución deja un celular de prueba retirado y una factura en efectivo.
