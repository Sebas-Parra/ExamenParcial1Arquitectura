$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & mvn -B -ntp -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'Maven no pudo compilar las pruebas del cliente de escritorio.' }
    & java -cp 'target/classes;target/lib/*' ec.edu.monster.pruebas.PruebaClienteRest
    if ($LASTEXITCODE -ne 0) { throw 'Falló una prueba funcional del cliente de escritorio.' }
} finally {
    Pop-Location
}
