# Diagramas del sistema en PowerDesigner

Cada categoría conserva un único modelo nativo editable de PowerDesigner (`.oom`). El modelo puede contener varios diagramas cuando describen flujos o vistas diferentes del mismo tipo.

| Carpeta | Modelo | Contenido |
|---|---|---|
| `01_CASOS_USO` | `CasosUsoCompleto.oom` | Una vista consolidada: `Casos de uso - Sistema completo`, con Vendedor y Banco BanQuito como actores relacionados con los casos que les corresponden. |
| `02_ACTIVIDAD` | `ActividadesSistema.oom` | Tres diagramas de flujo dentro del mismo modelo: facturar venta, otorgar crédito y verificar sujeto de crédito. |
| `03_SECUENCIA` | `SecuenciasSistema.oom` | Tres interacciones dentro del mismo modelo: verificar sujeto de crédito, facturar con crédito y consultar amortización. |
| `04_CLASES` | `ClasesSistema.oom` | Una vista consolidada (`Clases - Sistema completo`) con clases de ambas aplicaciones, asociaciones del dominio y dependencias de servicios. |
| `05_ARQUITECTURA` | `ArquitecturaCompleta.oom` | Cuatro clientes reales (consola, escritorio, web y móvil), API REST Comercializadora, API REST BanQuito y sus bases; la comercializadora integra el crédito con BanQuito. |

El modelo entidad-relación está en `02. MER\MER_SISTEMA_COMPLETO.cdm`. Incluye las nueve entidades de Comercializadora y BanQuito en grupos relacionados separados; el campo de crédito de las facturas representa la integración externa y no una clave foránea entre bases.

Los archivos `.puml` y las imágenes exportadas no forman parte de la entrega: los modelos de PowerDesigner son la fuente editable vigente.
