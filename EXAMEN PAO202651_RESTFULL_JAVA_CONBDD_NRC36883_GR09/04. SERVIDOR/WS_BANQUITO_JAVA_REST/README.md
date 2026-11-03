# Servidor REST del Core BanQuito (módulo de crédito)

Aplicación Java 21 empaquetada como WAR. Expone Jakarta RESTful Web Services (JAX-RS) con arquitectura MVC: `CreditResource` como controlador, `CreditoServices` para las reglas de negocio, `RepositorioBanquitoJdbc` para el acceso a datos y modelos Java serializados como JSON. Usa la base MariaDB/MySQL `banquito`.

Esta aplicación solo expone servicios web; los consume la Comercializadora de Teléfonos Celulares (`WS_COM_CEL_JAVA_REST` y sus clientes).

## Base de datos

El script está en `03. BDD/BANQUITO.sql`. Crea la base `banquito`, el usuario `banquito`/`banquito` y carga 6 clientes, 7 cuentas, 54 movimientos y un crédito activo. Ejecútelo como `root` desde HeidiSQL (Archivo > Cargar archivo SQL > Ejecutar) o, desde la carpeta del examen, en PowerShell:

```powershell
& "C:\Program Files\MariaDB 13.0\bin\mysql.exe" -u root -p -e "source 03. BDD/BANQUITO.sql"
```

Las fechas de los movimientos se calculan con `CURDATE()`, así que las reglas de "último mes" y "últimos 3 meses" se cumplen el día en que se carga el script. Vuelva a cargarlo antes de la defensa.

| Cód. | Cédula | Cliente | Escenario |
|---|---|---|---|
| 1 | 1712345678 | Ana Lucía Torres | Sujeto de crédito |
| 2 | 1723456789 | Carlos Andrés Mena | Sujeto de crédito |
| 3 | 1734567890 | Luis Fernando Paredes | Hombre menor de 25 años |
| 4 | 1745678901 | María José Castillo | Sin depósitos en el último mes |
| 5 | 1756789012 | Jorge Esteban Ruiz | Ya tiene un crédito activo (crédito 1) |
| 6 | 1767890123 | Sofía Valentina Vega | Sujeto de crédito con monto máximo de 225.00 |

## API

La raíz JAX-RS es `/api`. Con el despliegue por defecto en Payara la URL base es `http://localhost:8080/WS_BANQUITO_JAVA_REST/api`.

| Método y ruta | Descripción |
|---|---|
| `GET /api/credits/eligibility/{cedula}` | Verifica si la persona es sujeto de crédito. Devuelve `sujetoCredito` y `motivo`. |
| `GET /api/credits/max-amount/{codCliente}` | Devuelve `promedioDepositos`, `promedioRetiros` y `montoMaximo`. |
| `POST /api/credits` | Recibe `codCliente`, `precio` y `numeroCuotas`; otorga el crédito y devuelve la tabla de amortización. |
| `GET /api/credits/{codCredito}/amortization` | Devuelve el crédito y su `tablaAmortizacion`. |
| `GET /api/credits?cedula={cedula}` | Lista los `creditos` del cliente. |

### Otorgar crédito

```http
POST /WS_BANQUITO_JAVA_REST/api/credits
Content-Type: application/json

{"codCliente":1,"precio":450,"numeroCuotas":6}
```

Respuesta exitosa (HTTP 200), resumida:

```json
{
  "estado": "EXITO",
  "codigo": "OK",
  "mensaje": "Credito aprobado",
  "codCredito": 2,
  "codCliente": 1,
  "monto": 450.00,
  "numeroCuotas": 6,
  "tasaAnual": 16.50,
  "valorCuota": 78.65,
  "tablaAmortizacion": [
    {"numeroCuota": 1, "valorCuota": 78.65, "interesPagado": 6.19, "capitalPagado": 72.46, "saldo": 377.54}
  ]
}
```

## Reglas de negocio

**Sujeto de crédito** (por cédula), en este orden:
1. Debe ser cliente del banco.
2. Debe tener al menos un depósito (`TIPO = 'DEP'`) en el último mes.
3. Si es hombre (`GENERO = 'M'`), debe tener al menos 25 años.
4. No debe tener un crédito `ACTIVO`.

**Monto máximo** (por código de cliente): `((Promedio depósitos − Promedio retiros) × 30%) × 6` con los movimientos de los últimos 3 meses de todas sus cuentas. Si el resultado es negativo se devuelve 0. El enunciado dice 35% en el texto y 30% en la fórmula; el porcentaje se cambia con la propiedad JVM `banquito.credito.porcentaje` (por ejemplo `0.35`).

**Otorgar crédito:** el plazo va de 3 a 18 cuotas mensuales, la tasa es 16.5% anual y la cuota es fija:

```
Cuota = Monto × TasaPeriodo / (1 − (1 + TasaPeriodo)^−NúmeroCuotas),   TasaPeriodo = 16.5% / 12
```

La cuota se redondea a centavos; el interés, el capital y el saldo se calculan con precisión completa y se redondean al guardarlos, de modo que el ejemplo del enunciado (450 a 6 cuotas) se reproduce exactamente.

## Respuestas y errores

Todas las respuestas usan `estado`, `codigo` y `mensaje`; el resto de los campos se incluye cuando corresponde.

| Código | HTTP | Significado |
|---|---|---|
| `OK` | 200 | Operación correcta. Una persona que no es sujeto de crédito también responde `OK` con `sujetoCredito: false`. |
| `DATA-001` | 400 | Datos incompletos o fuera de rango. |
| `CRED-001` | 400 | El cliente no es sujeto de crédito (incluye `motivo`). |
| `CRED-002` | 400 | El precio supera el monto máximo (incluye `montoMaximo`). |
| `CLI-001` | 404 | Cliente inexistente. |
| `CRED-003` | 404 | Crédito inexistente. |
| `BD-001` | 500 | Error de base de datos. |

## Configuración

Propiedades JVM opcionales del servidor:

- `banquito.db.url` (por defecto `jdbc:mariadb://localhost:3306/banquito`)
- `banquito.db.usuario` (por defecto `banquito`)
- `banquito.db.clave` (por defecto `banquito`)
- `banquito.credito.porcentaje` (por defecto `0.30`)

El driver de MariaDB va dentro del WAR; no hace falta configurar un pool en Payara.

## Compilación

```powershell
mvn package
```

Despliegue `target/WS_BANQUITO_JAVA_REST-1.0-SNAPSHOT.war` en Payara (Jakarta EE 11).

## Pruebas

Pruebas locales de las reglas y de la tabla de amortización, sin base de datos ni Payara:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1 -SoloServicio
```

Con el WAR desplegado y la base recién cargada:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\ejecutar-pruebas.ps1
```

La prueba HTTP solo consulta datos (no otorga créditos), así que puede repetirse sin recargar la base.
