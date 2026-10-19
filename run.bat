@echo off
title Placement Management System - KTU S3 CS
echo ====================================================
echo Starting Placement Management System...
echo ====================================================

java -cp bin com.placementsystem.Main

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Application exited with an error code: %ERRORLEVEL%
    pause
)
