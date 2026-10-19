@echo off
title Build Placement Management System
echo Compiling Placement Management System Java Files...

if not exist bin mkdir bin

javac -encoding UTF-8 -d bin src/com/placementsystem/*.java src/com/placementsystem/model/*.java src/com/placementsystem/exception/*.java src/com/placementsystem/util/*.java src/com/placementsystem/gui/*.java

if %ERRORLEVEL% EQU 0 (
    echo Compilation successful. Output generated in /bin/
    echo You can now run run.bat
) else (
    echo Compilation failed. Check the error messages above.
)

pause
