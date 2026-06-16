# Sprint_MrNaina3

Structure du projet créée par l'assistant.

Arborescence recommandée:

Sprint_MrNaina/
├── lib/ (mettre servlet-api.jar ici)
├── src/ (sources Java)
└── WebContent/ (webapp)

Pour construire le WAR localement:

```bash
chmod +x scripts/build.sh
./scripts/build.sh
```

Remarques:
- Ajustez le chemin de `lib/servlet-api.jar` si nécessaire.
- Déployez le fichier `build/Sprint_MrNaina.war` dans un conteneur servlet (Tomcat, Jetty).

# Sprint 0

Projet de test Servlet + Tomcat.

Fonctionnalités :

- Servlet
- doGet()
- processRequest()
- web.xml
- Utilisation d'un jar
- Déploiement Tomcat