#!/bin/bash

PROJECT_NAME="Sprint_MrNaina3"
SERVLET_JAR="lib/servlet-api.jar"

rm -rf build
mkdir -p build/classes
mkdir -p build/webapp/WEB-INF/classes
mkdir -p WebContent/WEB-INF/lib

javac -d build/classes src/com/sprint0/Utilitaire.java

jar cf WebContent/WEB-INF/lib/utilitaire.jar \
-C build/classes com/sprint0/Utilitaire.class

javac -cp "$SERVLET_JAR:WebContent/WEB-INF/lib/utilitaire.jar" \
-d build/webapp/WEB-INF/classes \
src/com/sprint0/TestServlet.java

cp -r WebContent/* build/webapp/

cd build/webapp
jar cf "../${PROJECT_NAME}.war" .
cd ../..

echo "Build terminé : build/${PROJECT_NAME}.war"