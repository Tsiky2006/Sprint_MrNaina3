@echo off
setlocal enabledelayedexpansion

REM Script de compilation pour Windows
set PROJECT_ROOT=%~dp0..
set PROJECT_NAME=Sprint_MrNaina3
set SERVLET_JAR=%PROJECT_ROOT%\lib\servlet-api.jar

cd /d "%PROJECT_ROOT%"

REM Nettoyage
echo Nettoyage...
if exist build rmdir /s /q build
if exist framework.jar del /f framework.jar

REM Création des répertoires
echo Creation des repertoires...
mkdir build\framework-classes
mkdir build\test-classes
mkdir build\webapp\WEB-INF\classes
mkdir test-app\WebContent\WEB-INF\lib

REM Compilation du framework
echo.
echo ========================================
echo Compilation du framework...
echo ========================================

javac -cp "%SERVLET_JAR%" ^
-d build\framework-classes ^
framework\src\com\framework\annotation\*.java ^
framework\src\com\framework\core\*.java ^
framework\src\com\framework\util\*.java ^framework\src\com\framework\listener\*.java ^framework\src\com\framework\Model.java ^
framework\src\com\framework\FrontController.java

if errorlevel 1 (
    echo ERREUR: Compilation du framework echouee
    pause
    exit /b 1
)

echo Framework compile avec succes

REM Création du JAR du framework
echo.
echo Creation du framework.jar...
cd /d "%PROJECT_ROOT%\build\framework-classes"
jar cf "%PROJECT_ROOT%\framework.jar" -C . .
cd /d "%PROJECT_ROOT%"

if not exist framework.jar (
    echo ERREUR: Echec creation framework.jar
    pause
    exit /b 1
)

REM Copie du JAR dans test-app
copy framework.jar test-app\WebContent\WEB-INF\lib\

echo framework.jar cree et copie avec succes

REM Compilation de l'application test
echo.
echo ========================================
echo Compilation de l'application test...
echo ========================================

javac -cp "%SERVLET_JAR%;framework.jar" ^
-d build\test-classes ^
test-app\src\com\test\controller\*.java

if errorlevel 1 (
    echo ERREUR: Compilation test-app echouee
    pause
    exit /b 1
)

echo Application test compilée avec succes

REM Copie des classes compilées
echo.
echo Copie des classes compilees...
xcopy /E /I /Y build\test-classes\com build\webapp\WEB-INF\classes\com

REM Copie du contenu web
echo Copie du contenu web...
xcopy /E /I /Y test-app\WebContent\* build\webapp\

REM Création du WAR
echo.
echo ========================================
echo Creation du WAR...
echo ========================================

cd /d "%PROJECT_ROOT%\build\webapp"
jar cf "%PROJECT_ROOT%\build\%PROJECT_NAME%.war" -C . .
cd /d "%PROJECT_ROOT%"

if exist build\%PROJECT_NAME%.war (
    echo.
    echo ========================================
    echo BUILD REUSSI!
    echo ========================================
    echo WAR cree: build\%PROJECT_NAME%.war
    echo.
    pause
) else (
    echo ERREUR: Echec creation WAR
    pause
    exit /b 1
)
