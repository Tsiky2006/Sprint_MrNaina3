#!/bin/bash

PROJECT_NAME="Sprint_MrNaina3"
SERVLET_JAR="$HOME/Documents/tomcat11/lib/servlet-api.jar"

rm -rf build
rm -f framework.jar

mkdir -p build/framework-classes
mkdir -p build/test-classes
mkdir -p build/webapp/WEB-INF/classes
mkdir -p test-app/WebContent/WEB-INF/lib

echo "Compilation framework..."

javac -cp "$SERVLET_JAR:test-app/src" \
-d build/framework-classes \
framework/src/com/framework/annotation/*.java \
framework/src/com/framework/core/*.java \
framework/src/com/framework/util/*.java \
framework/src/com/framework/FrontController.java

if [ $? -ne 0 ]; then
    echo "Erreur compilation framework"
    exit 1
fi

echo "Creation framework.jar..."

jar cf framework.jar -C build/framework-classes .

cp framework.jar test-app/WebContent/WEB-INF/lib/

echo "Compilation application test..."

javac -cp "$SERVLET_JAR:framework.jar" \
-d build/test-classes \
test-app/src/com/test/controller/*.java

if [ $? -ne 0 ]; then
    echo "Erreur compilation test-app"
    exit 1
fi

cp -r build/test-classes/* build/webapp/WEB-INF/classes/

cp -r test-app/WebContent/* build/webapp/

echo "Creation WAR..."

cd build/webapp
jar cf "../${PROJECT_NAME}.war" .
cd ../..

echo ""
echo "Build terminé"
echo "WAR généré : build/${PROJECT_NAME}.war"