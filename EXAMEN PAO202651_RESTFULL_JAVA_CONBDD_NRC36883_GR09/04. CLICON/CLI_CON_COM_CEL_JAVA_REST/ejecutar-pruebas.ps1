$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & .\construir.ps1
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar el cliente de consola.' }
    & java -cp 'target/classes;target/lib/*' ec.edu.monster.pruebas.PruebaClienteRest
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba funcional del cliente de consola.' }
} finally {
    Pop-Location
}
