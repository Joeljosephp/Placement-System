@echo off
if not exist "bin" mkdir bin

echo Compiling sources and tests...
javac -encoding UTF-8 -d bin src/com/placementsystem/*.java src/com/placementsystem/model/*.java src/com/placementsystem/exception/*.java src/com/placementsystem/util/*.java src/com/placementsystem/gui/*.java test/*.java
if %errorlevel% neq 0 (
    echo Compilation failed.
    exit /b %errorlevel%
)

echo Running tests...
java -cp bin test.PlacementSystemTest %*
exit /b %errorlevel%
