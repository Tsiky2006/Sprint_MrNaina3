# Sprint_MrNaina3

Structure:

Sprint_MrNaina3/
├── framework/ (sources du mini framework MVC)
├── test-app/ (application de test)
├── lib/ (mettre servlet-api.jar ici)
└── scripts/

Pour construire le WAR localement:

```bash
chmod +x scripts/build.sh
./scripts/build.sh
```

Remarques:
- Le script construit `test-app/WebContent/WEB-INF/lib/framework.jar`.
- Déployez le fichier `build/Sprint_MrNaina3.war` dans un conteneur servlet (Tomcat, Jetty).

# Sprint 0

Projet de test Servlet + Tomcat.

Fonctionnalités :

- Servlet
- doGet()
- processRequest()
- web.xml
- Utilisation d'un jar
- Déploiement Tomcat
