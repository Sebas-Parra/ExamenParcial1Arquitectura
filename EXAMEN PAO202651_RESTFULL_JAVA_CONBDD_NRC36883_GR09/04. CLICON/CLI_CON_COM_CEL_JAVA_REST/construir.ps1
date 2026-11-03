param([string]$Maven)
$ErrorActionPreference = 'Stop'

if (-not $Maven) {
    $comandoMaven = Get-Command mvn.cmd, mvn -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($comandoMaven) { $Maven = $comandoMaven.Source }
}
if (-not $Maven -and $env:MAVEN_HOME) {
    $candidatoMaven = Join-Path $env:MAVEN_HOME 'bin/mvn.cmd'
    if (Test-Path -LiteralPath $candidatoMaven) { $Maven = $candidatoMaven }
}
if (-not $Maven) {
    $descargasNetBeans = Join-Path $env:USERPROFILE 'Downloads'
    $instalacionesNetBeans = @(Get-ChildItem -LiteralPath $descargasNetBeans -Directory -Filter 'netbeans*' -ErrorAction SilentlyContinue)
    foreach ($instalacionNetBeans in $instalacionesNetBeans) {
        $candidatoMaven = Join-Path $instalacionNetBeans.FullName 'netbeans/java/maven/bin/mvn.cmd'
        if (Test-Path -LiteralPath $candidatoMaven) { $Maven = $candidatoMaven; break }
    }
}
if (-not $Maven) {
    throw 'No se encontró Maven. Use NetBeans Clean and Build o indique -Maven con la ruta de mvn.cmd.'
}

Push-Location $PSScriptRoot
try {
    & $Maven -B -ntp -DskipTests package
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo compilar y empaquetar el cliente de consola.' }
    Write-Output 'Cliente compilado: target/CLI_CON_COM_CEL_JAVA_REST.jar (bibliotecas en target/lib)'
} finally {
    Pop-Location
}
