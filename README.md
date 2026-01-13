# Secure File Manager (CLI) – Java

## Contexte
Ce projet consiste à développer un **gestionnaire de fichiers sécurisé en ligne de commande (CLI)** en Java, sans interface graphique.  
Le développement est réalisé de manière **itérative**, avec des fonctionnalités ajoutées à chaque itération.
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

Exécution
```bash
java -cp bin Main
```
## Commandes disponibles (Itération 1)

help : affiche l’aide
pwd : affiche le répertoire courant (relatif au root autorisé)
ls : liste les fichiers et dossiers
cd <dossier> : se déplacer dans un sous-dossier
create <fichier> : créer un fichier
read <fichier> : lire le contenu d’un fichier
write <fichier> : écrire ou modifier un fichier
delete <fichier> : supprimer un fichier
exit : quitter l’application

## Gestion du périmètre autorisé

L’application fonctionne dans un répertoire racine autorisé défini au lancement.
Toute tentative de sortie de ce périmètre (ex : ../) est bloquée afin de garantir la sécurité du système de fichiers.


## Gestion des erreurs

Les erreurs courantes sont prises en compte :
- fichier ou dossier inexistant,
- permissions insuffisantes,
- commande invalide,
- tentative d’accès hors du répertoire autorisé.
Les messages affichés à l’utilisateur sont clairs et l’application ne s’arrête pas brutalement.

## Architecture du projet

Le projet suit une architecture en couches, inspirée de la Clean Architecture :

CLI → Application → Domain → Infrastructure
- `application` : logique métier et services
- `cli` : interface en ligne de commande
- `domain` : modèles de données et exceptions
- `infrastructures` : interaction avec le système de fichiers
- `Main.java` : point d’entrée de l’application

