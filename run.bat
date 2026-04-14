@echo off
echo ========================================
echo   Quan Ly Thu Vien So - Library Manager
echo ========================================
echo.

set JAVA_HOME=D:\java
set PATH=%JAVA_HOME%\bin;%PATH%

echo [*] Building and running...
call "%~dp0.maven\apache-maven-3.9.6\bin\mvn.cmd" javafx:run
