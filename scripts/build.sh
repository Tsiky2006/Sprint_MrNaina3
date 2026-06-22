#!/bin/bash
set -e

PROJECT_NAME="Sprint_MrNaina3"
SERVLET_JAR="$HOME/Documents/tomcat11/lib/servlet-api.jar"

rm -rf build
rm -f framework.jar
rm -f test-app/WebContent/WEB-INF/lib/framework.jar

mkdir -p build/framework-classes
mkdir -p build/test-classes
mkdir -p build/webapp/WEB-INF/classes
mkdir -p test-app/WebContent/WEB-INF/lib

echo "Compilation du framework..."

javac -cp "$SERVLET_JAR" \
-d build/framework-classes \
framework/src/com/framework/annotation/*.java \
framework/src/com/framework/util/*.java \
framework/src/com/framework/FrontController.java

echo "Création framework.jar..."

jar cf framework.jar -C build/framework-classes .

cp framework.jar test-app/WebContent/WEB-INF/lib/

echo "Compilation application test..."

javac -cp "$SERVLET_JAR:framework.jar" \
-d build/test-classes \
test-app/src/com/test/controller/*.java

cp -r build/test-classes/* build/webapp/WEB-INF/classes/
cp -r test-app/WebContent/* build/webapp/

cd build/webapp
jar cf "../${PROJECT_NAME}.war" .
cd ../..

echo "Build terminé : build/${PROJECT_NAME}.war"