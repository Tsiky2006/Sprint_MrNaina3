@echo off
setlocal

cd /d "%~dp0.."

REM Verification des bibliotheques
if not exist "lib\servlet-api.jar" (
    echo ERREUR : lib\servlet-api.jar introuvable.
    exit /b 1
)

if not exist "lib\gson-2.11.0.jar" (
    echo ERREUR : lib\gson-2.11.0.jar introuvable.
    exit /b 1
)

REM Nettoyage
echo Nettoyage...
if exist "build" rmdir /s /q "build"

REM Creation des repertoires
mkdir "build\framework-classes"
mkdir "build\test-classes"
mkdir "build\webapp\WEB-INF\classes"

if not exist "test-app\WebContent\WEB-INF\lib" (
    mkdir "test-app\WebContent\WEB-INF\lib"
)

REM Compilation du framework avec Gson
echo Compilation du framework...

javac -encoding UTF-8 ^
-cp "lib\servlet-api.jar;lib\gson-2.11.0.jar" ^
-d "build\framework-classes" ^
framework\src\com\framework\annotation\*.java ^
framework\src\com\framework\core\*.java ^
framework\src\com\framework\util\*.java ^
framework\src\com\framework\listener\*.java ^
framework\src\com\framework\FrontController.java

if errorlevel 1 goto :error

REM Creation du JAR du framework
echo Creation du framework.jar...

jar cf "framework.jar" -C "build\framework-classes" .
if errorlevel 1 goto :error

REM Copie du framework et de Gson
copy /Y "framework.jar" "test-app\WebContent\WEB-INF\lib\"
if errorlevel 1 goto :error

copy /Y "lib\gson-2.11.0.jar" "test-app\WebContent\WEB-INF\lib\"
if errorlevel 1 goto :error

REM Compilation du DTO et des controleurs
echo Compilation de l'application test...

javac -encoding UTF-8 ^
-cp "lib\servlet-api.jar;framework.jar;lib\gson-2.11.0.jar" ^
-d "build\test-classes" ^
test-app\src\com\test\dto\*.java ^
test-app\src\com\test\controller\*.java

if errorlevel 1 goto :error

REM Copie des classes compilees
xcopy "build\test-classes\*" "build\webapp\WEB-INF\classes\" /E /I /Y
if errorlevel 1 goto :error

REM Copie des vues, du web.xml et des bibliotheques
xcopy "test-app\WebContent\*" "build\webapp\" /E /I /Y
if errorlevel 1 goto :error

REM Creation du WAR
echo Creation du WAR...

jar cf "build\Sprint_MrNaina3.war" -C "build\webapp" .
if errorlevel 1 goto :error

echo.
echo BUILD REUSSI : build\Sprint_MrNaina3.war
exit /b 0

:error
echo.
echo ECHEC : verifier les erreurs ci-dessus.
exit /b 1