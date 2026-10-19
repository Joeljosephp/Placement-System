@echo off
title Placement Management System
echo Starting Placement Management System...

java -cp bin com.placementsystem.Main %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error code: %ERRORLEVEL%
    pause
)
