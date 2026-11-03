# Diagramas UML del examen

Cada diagrama tiene su fuente PlantUML (`.puml`) y la imagen generada (`.png`). Los modelos entidad-relación están en `02. MER`.

| Carpeta | Diagramas |
|---|---|
| `01_CASOS_USO` | `CasosUsoModuloCredito` (rúbrica: módulo de crédito BanQuito) y `CasosUsoComercializadora` |
| `02_ACTIVIDAD` | `ActividadVerificarSujetoCredito`, `ActividadOtorgarCredito`, `ActividadFacturarVenta` |
| `03_SECUENCIA` | `SecuenciaVerificarSujetoCredito`, `SecuenciaFacturarCredito`, `SecuenciaConsultarAmortizacion` |
| `04_CLASES` | `DiagramaClasesBanquito`, `DiagramaClasesComercializadora` |
| `05_ARQUITECTURA` | `DiagramaArquitectura` (rúbrica: arquitectura de la solución completa) |

## Regenerar las imágenes

Se necesita Java, [PlantUML](https://plantuml.com/download) y Graphviz (`dot`). Desde la carpeta del examen:

```powershell
java -jar plantuml.jar -charset UTF-8 -tpng "01. UML/0.3 DIAGRAMAS/**.puml" "02. MER/*.puml"
```

Si Graphviz no está en el `PATH`, agregue `-graphvizdot "C:\ruta\Graphviz\bin\dot.exe"`. El plugin PlantUML de NetBeans, IntelliJ o VS Code también muestra los `.puml` directamente.
