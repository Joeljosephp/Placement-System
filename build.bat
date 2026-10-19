@echo off
title Build Placement Management System
echo ====================================================
echo Compiling Placement Management System Java Files...
echo ====================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -d bin src/com/placementsystem/*.java src/com/placementsystem/model/*.java src/com/placementsystem/exception/*.java src/com/placementsystem/util/*.java src/com/placementsystem/gui/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================================
    echo Compilation successful! Output generated in /bin/
    echo You can now run run.bat
    echo ====================================================
) else (
    echo.
    echo Compilation failed! Please check the error messages above.
)

pause
