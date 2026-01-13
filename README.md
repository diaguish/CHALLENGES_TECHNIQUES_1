# Secure File Manager (CLI) – Java

## Contexte
Gestionnaire de fichiers sécurisé en ligne de commande (Java).  
Développement itératif :
- Itération 1 : CRUD + navigation dans un répertoire autorisé + erreurs + CLI
- Itération 2 : intégrité (hash) + traçabilité (logs)
- Itération 3 : chiffrement + gestion des clés

## Prérequis
- Java 17+

## Compilation
Depuis le dossier `Livrables` :
```bash
javac -d ./bin/ ./src/Main.java ./src/application/*.java ./src/cli/*.java ./src/domain/exception/*.java ./src/domain/repository/*.java ./src/domain/model/*.java ./src/infrastructures/filesystem/*.java

## Exécution
```bash
java -cp bin Main
```

