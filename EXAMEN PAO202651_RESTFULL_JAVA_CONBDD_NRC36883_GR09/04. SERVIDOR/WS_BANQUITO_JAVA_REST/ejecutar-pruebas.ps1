param(
    [string]$BaseUrl = "http://localhost:8080/WS_BANQUITO_JAVA_REST/api",
    [switch]$SoloServicio
)

$ErrorActionPreference = "Stop"
$projectDirectory = $PSScriptRoot
Push-Location $projectDirectory

try {
    mvn -DskipTests test-compile
    if ($LASTEXITCODE -ne 0) {
        throw "Maven no pudo compilar las pruebas (código $LASTEXITCODE)."
    }

    $testClasspath = "target\test-classes;target\classes"
    java -cp $testClasspath ec.edu.monster.pruebas.PruebaServicioCredito
    if ($LASTEXITCODE -ne 0) {
        throw "Fallaron las pruebas locales del servicio de crédito (código $LASTEXITCODE)."
    }

    if (-not $SoloServicio) {
        java -cp $testClasspath ec.edu.monster.pruebas.PruebaIntegracionREST $BaseUrl
        if ($LASTEXITCODE -ne 0) {
            throw "Falló la prueba HTTP REST. Confirma que el WAR esté desplegado, que la base esté cargada con BANQUITO.sql y que BaseUrl sea correcto."
        }
    }
}
finally {
    Pop-Location
}
