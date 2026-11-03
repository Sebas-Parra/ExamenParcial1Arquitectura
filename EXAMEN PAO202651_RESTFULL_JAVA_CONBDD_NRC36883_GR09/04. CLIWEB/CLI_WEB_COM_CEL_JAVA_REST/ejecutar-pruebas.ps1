$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & mvn -B -ntp -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Maven no pudo compilar las pruebas del cliente web.' }
    & java -cp 'target/classes;target/pruebas-lib/*' ec.edu.monster.pruebas.PruebaClienteRest
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba funcional del cliente web.' }
} finally {
    Pop-Location
}
