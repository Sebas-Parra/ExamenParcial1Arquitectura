$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    & .\gradlew.bat :app:testDebugUnitTest
    if ($LASTEXITCODE -ne 0) { throw 'Fallaron las pruebas locales del cliente móvil.' }
} finally {
    Pop-Location
}
