@echo off
setlocal
cd /d "%~dp0.."

if not exist "lib\servlet-api.jar" (
    echo ERREUR : servlet-api.jar introuvable.
    exit /b 1
)

if not exist "lib\gson-2.11.0.jar" (
    echo ERREUR : gson-2.11.0.jar introuvable.
    exit /b 1
)

echo Nettoyage...
if exist "build" rmdir /s /q "build"
if exist "build" goto :error

mkdir "build\framework-classes"
mkdir "build\test-classes"
mkdir "build\webapp\WEB-INF\classes"

if not exist "test-app\WebContent\WEB-INF\lib" (
    mkdir "test-app\WebContent\WEB-INF\lib"
)

echo Compilation du framework...
javac --release 17 -encoding UTF-8 -cp "lib\servlet-api.jar;lib\gson-2.11.0.jar" -d "build\framework-classes" framework\src\com\framework\annotation\*.java framework\src\com\framework\core\*.java framework\src\com\framework\util\*.java framework\src\com\framework\listener\*.java framework\src\com\framework\FrontController.java
if errorlevel 1 goto :error

echo Creation du framework.jar...
jar cf "framework.jar" -C "build\framework-classes" .
if errorlevel 1 goto :error

copy /Y "framework.jar" "test-app\WebContent\WEB-INF\lib\"
if errorlevel 1 goto :error

copy /Y "lib\gson-2.11.0.jar" "test-app\WebContent\WEB-INF\lib\"
if errorlevel 1 goto :error

echo Compilation de l'application test...

REM Chemins relatifs avec des slashs pour la liste javac.
(for /r "test-app\src" %%F in (*.java) do (
    for %%R in ("test-app\src") do (
        set "JAVA_SOURCE=%%F"
        call :source
    )
)) > "build\test-sources.txt"

javac --release 17 -encoding UTF-8 -cp "lib\servlet-api.jar;framework.jar;lib\gson-2.11.0.jar" -d "build\test-classes" @"build\test-sources.txt"
if errorlevel 1 goto :error

xcopy "build\test-classes\*" "build\webapp\WEB-INF\classes\" /E /I /Y
if errorlevel 1 goto :error

xcopy "test-app\WebContent\*" "build\webapp\" /E /I /Y
if errorlevel 1 goto :error

echo Creation du WAR...
jar cf "build\Sprint_MrNaina3.war" -C "build\webapp" .
if errorlevel 1 goto :error

echo BUILD REUSSI : build\Sprint_MrNaina3.war
exit /b 0

:source
set "JAVA_SOURCE=%JAVA_SOURCE:\=/%"
echo "%JAVA_SOURCE%"
exit /b 0

:error
echo ECHEC : verifier les erreurs ci-dessus.
exit /b 1