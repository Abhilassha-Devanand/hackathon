@echo off
set DIRNAME=%~dp0
set MVN_BIN=%DIRNAME%\.mvn\maven\apache-maven-3.9.8\bin\mvn.cmd
if exist "%MVN_BIN%" (
    "%MVN_BIN%" %*
) else (
    echo Maven binary not found at %MVN_BIN%
    exit /b 1
)
